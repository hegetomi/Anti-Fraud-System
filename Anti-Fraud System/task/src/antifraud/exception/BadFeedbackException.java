package antifraud.exception;

public class BadFeedbackException extends Exception{

    private Integer errorCode;

    public BadFeedbackException(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
