package antifraud.dto;

import antifraud.enums.State;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class TransactionFeedbackDto {

    @NotNull
    private Long transactionId;

    @NotNull
    private State feedback;

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public State getFeedback() {
        return feedback;
    }

    public void setFeedback(State feedback) {
        this.feedback = feedback;
    }
}
