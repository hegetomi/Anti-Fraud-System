package antifraud.mapper;

import antifraud.dto.SuspiciousIpDto;
import antifraud.model.SuspiciousIp;
import org.springframework.stereotype.Component;

@Component
public class SuspiciousIpMapper {

    public SuspiciousIpDto modelToDto(SuspiciousIp sus) {
        SuspiciousIpDto dto = new SuspiciousIpDto();
        dto.setId(sus.getId());
        dto.setIp(sus.getIp());
        return dto;
    }
}
