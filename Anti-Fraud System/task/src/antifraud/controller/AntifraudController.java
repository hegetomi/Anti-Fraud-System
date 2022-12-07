package antifraud.controller;

import antifraud.dto.*;
import antifraud.exception.BadFeedbackException;
import antifraud.exception.BadRequestParamException;
import antifraud.mapper.StolenCardMapper;
import antifraud.mapper.SuspiciousIpMapper;
import antifraud.mapper.TransactionMapper;
import antifraud.service.AntifraudService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityExistsException;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/antifraud")
public class AntifraudController {

    @Autowired
    AntifraudService antifraudService;
    @Autowired
    SuspiciousIpMapper ipMapper;
    @Autowired
    StolenCardMapper cardMapper;
    @Autowired
    TransactionMapper transactionMapper;


    @PostMapping("/transaction")
    public TransactionResultDto postTransaction(@RequestBody @Valid TransactionDto transactionDto) {
        try {
            return antifraudService.checkTransaction(transactionDto);
        } catch (BadRequestParamException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("/suspicious-ip")
    public SuspiciousIpDto postIp(@RequestBody @Valid SuspiciousIpDto suspiciousIpDto) {
        try {
            return ipMapper.modelToDto(antifraudService.saveSuspiciousIp(suspiciousIpDto.getIp()));
        } catch (BadRequestParamException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (EntityExistsException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(409));
        }
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    public ResponseEntity<Map<String, String>> deleteIp(@PathVariable String ip) {
        try {
            antifraudService.deleteIp(ip);
            return ResponseEntity.ok(Map.of("status", "IP " + ip + " successfully removed!"));
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (BadRequestParamException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/suspicious-ip")
    public List<SuspiciousIpDto> getAllIp() {
        return antifraudService.getAllIp().stream()
                .map(ip -> ipMapper.modelToDto(ip))
                .collect(Collectors.toList());
    }

    @PostMapping("/stolencard")
    public StolenCardDto postCard(@RequestBody @Valid StolenCardDto dto) {
        try {
            return cardMapper.modelToDto(antifraudService.saveCard(dto));
        } catch (BadRequestParamException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (EntityExistsException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(409));
        }
    }

    @DeleteMapping("/stolencard/{number}")
    public ResponseEntity<Map<String, String>> deleteCard(@PathVariable("number") @NotBlank @Size(min = 16, max = 16) String number) {
        try {
            antifraudService.deleteCard(number);
            return ResponseEntity.ok(Map.of("status", "Card " + number + " successfully removed!"));
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (BadRequestParamException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/stolencard")
    public List<StolenCardDto> getStolenCards() {
        return antifraudService.getAllCards().stream()
                .map(card -> cardMapper.modelToDto(card))
                .collect(Collectors.toList());
    }

    @PutMapping("/transaction")
    public ResponseEntity<TransactionDetailsDto> putFeedback(@RequestBody @Valid TransactionFeedbackDto dto) {

        try {
            return ResponseEntity.ok(transactionMapper.modelToDto(
                    antifraudService.saveFeedback(dto.getTransactionId(), dto.getFeedback())));
        } catch (BadFeedbackException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getErrorCode()));
        }
    }

    @GetMapping("/history")
    public List<TransactionDetailsDto> getAllTransaction() {
        return antifraudService.getAllTransaction().stream()
                .map(transactionMapper::modelToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/history/{number}")
    public List<TransactionDetailsDto> getTransactionsByCard(@PathVariable String number) {

        try {
            List<TransactionDetailsDto> collect = antifraudService.findTransactionsForCard(number).stream()
                    .map(transactionMapper::modelToDto).collect(Collectors.toList());

            if (collect.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            return collect;
        } catch (BadRequestParamException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
