package antifraud.mapper;

import antifraud.dto.TransactionDetailsDto;
import antifraud.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionDetailsDto modelToDto(Transaction transaction) {

        TransactionDetailsDto dto = new TransactionDetailsDto();
        dto.setTransactionId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setIp(transaction.getIp());
        dto.setNumber(transaction.getNumber());
        dto.setRegion(transaction.getRegion());
        dto.setDate(transaction.getLocalDateTime());
        dto.setResult(transaction.getState());
        if(transaction.getFeedback() != null) {
            dto.setFeedback(transaction.getFeedback().toString());
        } else {
            dto.setFeedback("");
        }
        return dto;

    }

}
