package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import javax.validation.constraints.Size;

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
	
	@Size(min = 0, max = 10000)
	private String warning;
	
	public PublicationCreationResDTO() {
		super();
	}

	public PublicationCreationResDTO(final LogTraceInfoDTO traceInfo,final String inWarning) {
		super(traceInfo);
		warning = inWarning;
	}
	
}
