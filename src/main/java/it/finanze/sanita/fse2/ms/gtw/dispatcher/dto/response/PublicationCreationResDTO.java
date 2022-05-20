package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author CPIERASC
 *
 *	DTO used to return validation result.
 */
@Getter
@Setter
public class PublicationCreationResDTO extends ResponseDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -1550024371939901939L;
	
	public PublicationCreationResDTO() {
		super();
	}

	public PublicationCreationResDTO(final LogTraceInfoDTO traceInfo) {
		super(traceInfo);
	}
	
}
