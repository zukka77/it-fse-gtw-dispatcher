package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EdsTraceResponseDTO extends ResponseDTO {

	private Boolean esito;

	private String errorMessage;

	public EdsTraceResponseDTO() {
		super();
	}

	public EdsTraceResponseDTO(final LogTraceInfoDTO traceInfo, final Boolean inEsito, final String inErrorMessage) {
		super(traceInfo);
		esito = inEsito;
		errorMessage = inErrorMessage;
	}

}
