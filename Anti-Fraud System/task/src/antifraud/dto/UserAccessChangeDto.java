package antifraud.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class UserAccessChangeDto {
    @NotNull
    @NotBlank
    @NotEmpty
    private String username;
    private AccessOperation operation;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public AccessOperation getOperation() {
        return operation;
    }

    public void setOperation(AccessOperation operation) {
        this.operation = operation;
    }
}
