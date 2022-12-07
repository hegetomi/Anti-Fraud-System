package antifraud.mapper;

import antifraud.dto.StolenCardDto;
import antifraud.model.StolenCard;
import org.springframework.stereotype.Component;

@Component
public class StolenCardMapper {

    public StolenCardDto modelToDto(StolenCard card) {
        StolenCardDto dto = new StolenCardDto();
        dto.setId(card.getId());
        dto.setNumber(card.getNumber()+"");
        return dto;
    }

}
