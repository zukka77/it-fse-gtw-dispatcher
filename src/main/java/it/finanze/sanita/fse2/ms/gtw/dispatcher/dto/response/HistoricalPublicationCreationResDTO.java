package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import lombok.Getter;
import lombok.Setter;

/**
 *
 *	DTO used to return Historical Document publication result.
 */
@Getter
@Setter
public class HistoricalPublicationCreationResDTO extends ResponseDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -1550024441939901939L;
	
	public HistoricalPublicationCreationResDTO() {
		super();
	}

	public HistoricalPublicationCreationResDTO(final LogTraceInfoDTO traceInfo) {
		super(traceInfo);
	}
	
}
