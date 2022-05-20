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
public class ValidationCDAResDTO extends ResponseDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -2144353497297675698L;
	
	private String transactionId;

	public ValidationCDAResDTO() {
		super();
	}

	public ValidationCDAResDTO(final LogTraceInfoDTO traceInfo, final String inTransactionId) {
		super(traceInfo);
		transactionId = inTransactionId;
	}
	
}
