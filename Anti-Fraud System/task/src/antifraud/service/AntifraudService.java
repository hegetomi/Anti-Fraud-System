package antifraud.service;

import antifraud.dto.StolenCardDto;
import antifraud.dto.TransactionDto;
import antifraud.dto.TransactionResultDto;
import antifraud.enums.State;
import antifraud.exception.BadFeedbackException;
import antifraud.exception.BadRequestParamException;
import antifraud.model.Limit;
import antifraud.model.StolenCard;
import antifraud.model.SuspiciousIp;
import antifraud.model.Transaction;
import antifraud.repository.LimitRepository;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.SuspiciousIpRepository;
import antifraud.repository.TransactionRepository;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class AntifraudService {
    @Autowired
    private SuspiciousIpRepository suspiciousIpRepository;
    @Autowired
    private StolenCardRepository stolenCardRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LimitRepository limitRepository;

    //private Long allowedLimit = 200l;
    //private Long manualLimit = 1500l;

    private Logger LOGGER = LoggerFactory.getLogger(AntifraudService.class);

    public TransactionResultDto checkTransaction(TransactionDto transactionDto) throws BadRequestParamException {
        Long amount = transactionDto.getAmount();
        Limit limit = getLimit();
        LOGGER.warn("LIMITID " + limit.getId());


        TransactionResultDto resultDto = new TransactionResultDto();
        List<String> reasons = new ArrayList<>();
        if (amount == null || amount <= 0 || transactionDto.getNumber() == null || transactionDto.getIp() == null) {
            throw new BadRequestParamException();
        }
        boolean isValidIp = checkIpValidity(transactionDto.getIp());
        boolean isValidCard = checkCardValidity(transactionDto.getNumber());
        if (!isValidCard || !isValidIp) {
            throw new BadRequestParamException();
        }
        Optional<StolenCard> stolenCard = stolenCardRepository.findByNumber(transactionDto.getNumber());
        Optional<SuspiciousIp> susIp = suspiciousIpRepository.findByIp(transactionDto.getIp());
        String region = transactionDto.getRegion();
        String ip = transactionDto.getIp();
        LocalDateTime to = transactionDto.getDate();
        LocalDateTime from = transactionDto.getDate().minusHours(1);

        long listOfTransactionsRegion = transactionRepository
                .findDistinctByRegionNotLikeAndLocalDateTimeBetween(region, transactionDto.getNumber(), from, to);
        long listOfTransactionIp = transactionRepository
                .findDistinctByIpNotLikeAndLocalDateTimeBetween(ip, transactionDto.getNumber(), from, to);
        if (stolenCard.isPresent()) {
            reasons.add("card-number");
        }
        if (susIp.isPresent()) {
            reasons.add("ip");
        }
        if (amount > limit.getManualLimit()) {
            reasons.add("amount");
        }
        if (listOfTransactionsRegion > 2) {
            reasons.add("region-correlation");
        }
        if (listOfTransactionIp > 2) {
            reasons.add("ip-correlation");
        }

        if (!reasons.isEmpty()) {
            resultDto.setResult("PROHIBITED");
        }

        LOGGER.warn("AMOUNT: " + amount);
        LOGGER.warn("ALLOWED_AMOUNT: " + limit.getAllowedLimit());
        LOGGER.warn("MANUAL_AMOUNT: " + limit.getManualLimit());

        if (null == resultDto.getResult()) {
            if (amount > limit.getAllowedLimit() && amount <= limit.getManualLimit()) {
                reasons.add("amount");
                resultDto.setResult("MANUAL_PROCESSING");
            }
            if (listOfTransactionsRegion == 2) {
                reasons.add("region-correlation");
                resultDto.setResult("MANUAL_PROCESSING");
            }
            if (listOfTransactionIp == 2) {
                reasons.add("ip-correlation");
                resultDto.setResult("MANUAL_PROCESSING");
            }
        }
        if (null == resultDto.getResult()) {
            resultDto.setResult("ALLOWED");
        }

        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDto.getAmount());
        transaction.setIp(transactionDto.getIp());
        transaction.setNumber(transactionDto.getNumber());
        transaction.setLocalDateTime(transactionDto.getDate());
        transaction.setRegion(transactionDto.getRegion());
        transaction.setState(State.valueOf(resultDto.getResult()));
        transactionRepository.save(transaction);

        resultDto.setInfo(mapArrayToString(reasons));
        return resultDto;


    }

    private Limit getLimit() {
        Optional<Limit> existingLimit = limitRepository.findById(9999L);
        if(existingLimit.isEmpty()) {
            LOGGER.warn("INITLIMIT CALLED");
            Limit limit = new Limit();
            limit.setId(9999L);
            limit.setAllowedLimit(200L);
            limit.setManualLimit(1500L);
            limit.setAtDate(LocalDateTime.now());
            return limitRepository.save(limit);
        } else {
            return existingLimit.get();
        }
    }

    private String mapArrayToString(List<String> reasons) {
        StringBuilder builder = new StringBuilder();

        reasons.sort(Comparator.naturalOrder());

        if (reasons.isEmpty()) {
            return "none";
        }

        for (int i = 0; i < reasons.size(); i++) {
            builder.append(reasons.get(i));
            if (i != reasons.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    @Transactional
    public SuspiciousIp saveSuspiciousIp(String ip) throws EntityExistsException, BadRequestParamException {
        Optional<SuspiciousIp> ipOptional = suspiciousIpRepository.findByIp(ip);
        boolean isValidIp = checkIpValidity(ip);
        if (!isValidIp) {
            throw new BadRequestParamException();
        }
        if (ipOptional.isPresent()) {
            throw new EntityExistsException();
        }

        SuspiciousIp suspiciousIp = new SuspiciousIp();
        suspiciousIp.setIp(ip);
        return suspiciousIpRepository.save(suspiciousIp);
    }

    private boolean checkIpValidity(String ip) {
        String[] ipRanges = ip.split("\\.");
        if (ipRanges.length != 4) {
            return false;
        }
        for (String ipRange : ipRanges) {
            try {
                int rangeNumber = Integer.parseInt(ipRange);
                if (rangeNumber < 0 || rangeNumber > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }

        }
        return true;
    }

    @Transactional
    public void deleteIp(String ip) throws NotFoundException, BadRequestParamException {
        boolean isValidIp = checkIpValidity(ip);
        if (!isValidIp) {
            throw new BadRequestParamException();
        }
        Optional<SuspiciousIp> suspiciousIp = suspiciousIpRepository.findByIp(ip);
        if (suspiciousIp.isEmpty()) {
            throw new NotFoundException("");
        }
        suspiciousIpRepository.deleteById(suspiciousIp.get().getId());
    }

    public List<SuspiciousIp> getAllIp() {
        return suspiciousIpRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Transactional
    public StolenCard saveCard(StolenCardDto dto) throws BadRequestParamException, EntityExistsException {
        boolean isValidCard = checkCardValidity(dto.getNumber());
        LOGGER.warn(dto.getNumber() + "");
        LOGGER.warn(isValidCard + "");

        if (!isValidCard) {
            throw new BadRequestParamException();
        }
        Optional<StolenCard> ipOptional = stolenCardRepository.findByNumber(dto.getNumber());
        LOGGER.warn(ipOptional.isPresent() + "");
        if (ipOptional.isPresent()) {
            throw new EntityExistsException();
        }

        StolenCard stolenCard = new StolenCard();
        stolenCard.setNumber(dto.getNumber());
        return stolenCardRepository.save(stolenCard);
    }

    private boolean checkCardValidity(String number) {
        if (number.matches("[a-zA-Z]") || number.length() != 16) {
            return false;
        }
        String numAsString = number;
        int checkSum = Integer.parseInt(String.valueOf(numAsString.charAt(15)));
        numAsString = numAsString.substring(0, 15);

        int sumOfNums = luhnAlgorithm(numAsString, 0);
        return sumOfNums % 10 == 0 && checkSum == 0 || 10 - (sumOfNums % 10) == checkSum;


    }

    private int luhnAlgorithm(String numAsString, int sumOfNums) {
        LOGGER.warn("numasstring: " + numAsString);
        String[] numsArray = numAsString.split("");
        for (int i = 0; i < numsArray.length; i++) {
            int currentAsNumber = Integer.parseInt((numsArray[i]));
            if (i % 2 == 0) {
                numsArray[i] = String.valueOf(currentAsNumber * 2);
            }
            currentAsNumber = Integer.parseInt((numsArray[i]));
            if (currentAsNumber > 9) {
                numsArray[i] = String.valueOf(currentAsNumber - 9);
            }
            currentAsNumber = Integer.parseInt((numsArray[i]));
            sumOfNums = sumOfNums + currentAsNumber;
        }
        LOGGER.warn("sumofnums: " + sumOfNums);
        return sumOfNums;
    }

    @Transactional
    public void deleteCard(String number) throws BadRequestParamException, NotFoundException {
        boolean isValidIp = checkCardValidity(number);
        if (!isValidIp) {
            throw new BadRequestParamException();
        }
        Optional<StolenCard> stolenCard = stolenCardRepository.findByNumber(number);
        if (stolenCard.isEmpty()) {
            throw new NotFoundException("");
        }
        stolenCardRepository.deleteById(stolenCard.get().getId());
    }

    public List<StolenCard> getAllCards() {
        return stolenCardRepository.findAll();
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    @Transactional
    public Transaction saveFeedback(Long transactionId, State feedback) throws BadFeedbackException {
        Optional<Transaction> transactionOptional = transactionRepository.findById(transactionId);

        if (transactionOptional.isPresent()) {
            Transaction transaction = transactionOptional.get();
            if (transaction.getFeedback() != null) {
                //HttpStatus.CONFLICT
                throw new BadFeedbackException(409);
            }
            if (feedback.equals(transaction.getState())) {
                //HttpStatus.UNPROCESSABLE_ENTITY
                throw new BadFeedbackException(422);
            }
            modifyLimits(transaction.getState(), feedback, transaction.getAmount());
            transaction.setFeedback(feedback);
            return transaction;
        } else {
            //404
            throw new BadFeedbackException(404);
        }

    }

    @Transactional
    protected void modifyLimits(State state, State feedback, Long amount) {
        Limit limit = getLimit();
        LOGGER.warn("LIMITID " + limit.getId());
        LOGGER.warn("MODIFYING LIMITS");
        LOGGER.warn("TRIGGER: " + amount);
        LOGGER.warn("OLDALLOWED: " +limit.getAllowedLimit());
        LOGGER.warn("OLDMANUAL: " + limit.getManualLimit());
        if (state.equals(State.ALLOWED) && feedback.equals(State.PROHIBITED)) {
            limit.setAllowedLimit(lowerAllowed(limit.getAllowedLimit(),amount));
            limit.setManualLimit(lowerManual(limit.getManualLimit(),amount));
        } else if (state.equals(State.ALLOWED) && feedback.equals(State.MANUAL_PROCESSING)) {
            limit.setAllowedLimit(lowerAllowed(limit.getAllowedLimit(),amount));
        } else if (state.equals(State.MANUAL_PROCESSING) && feedback.equals(State.ALLOWED)) {
            limit.setAllowedLimit(increaseAllowed(limit.getAllowedLimit(),amount));
        } else if (state.equals(State.MANUAL_PROCESSING) && feedback.equals(State.PROHIBITED)) {
            limit.setManualLimit(lowerManual(limit.getManualLimit(),amount));
        } else if (state.equals(State.PROHIBITED) && feedback.equals(State.ALLOWED)) {
            limit.setAllowedLimit(increaseAllowed(limit.getAllowedLimit(),amount));
            limit.setManualLimit(increaseManual(limit.getManualLimit(),amount));
        } else if (state.equals(State.PROHIBITED) && feedback.equals(State.MANUAL_PROCESSING)) {
            limit.setManualLimit(increaseManual(limit.getManualLimit(),amount));
        }
        LOGGER.warn("NEWALLOWED: " + limit.getAllowedLimit());
        LOGGER.warn("NEWMANUAL: " + limit.getManualLimit());
    }

    private Long lowerAllowed(Long allowedLimit, Long amount) {
        return (long) Math.ceil(0.8 * allowedLimit - 0.2 * amount);
    }

    private Long lowerManual(Long allowedLimit, Long amount) {
        return (long) Math.ceil(0.8 * allowedLimit - 0.2 * amount);
    }

    private Long increaseAllowed(Long allowedLimit, Long amount) {
       return (long) Math.ceil(0.8 * allowedLimit + 0.2 * amount);
    }

    private Long increaseManual(Long allowedLimit, Long amount) {
        return (long) Math.ceil(0.8 * allowedLimit + 0.2 * amount);
    }

    public List<Transaction> getAllTransaction() {
        return transactionRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }



    public List<Transaction> findTransactionsForCard(String number) throws BadRequestParamException {
        if (!checkCardValidity(number)) {
            throw new BadRequestParamException();
        }
        return transactionRepository.findByNumber(number);
    }
}
