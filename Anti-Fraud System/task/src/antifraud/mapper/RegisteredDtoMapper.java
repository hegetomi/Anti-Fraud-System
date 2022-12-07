package antifraud.mapper;

import antifraud.dto.RegisteredDto;
import antifraud.model.User;
import org.springframework.stereotype.Component;

@Component
public class RegisteredDtoMapper {

    public RegisteredDto modelToDto(User user) {
        RegisteredDto dto = new RegisteredDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setUsername(user.getUserName());
        dto.setRole(user.getRole());
        return dto;
    }

}
