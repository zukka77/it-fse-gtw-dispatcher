package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PublicationResultEnum;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PublicationOutputDTO {
	private String msg;
    private PublicationResultEnum result;
}
