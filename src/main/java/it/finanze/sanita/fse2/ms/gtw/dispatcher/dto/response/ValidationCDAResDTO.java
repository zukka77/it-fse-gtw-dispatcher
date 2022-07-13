package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
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
	
	@Size(min = 0, max = 256)
	@Schema(description = "Identificativo univoco della transazione")
	private String workflowInstanceId;
	
	@Size(min = 0, max = 10000)
	@Schema(description = "Dettaglio del warning")
	private String warning;

	public ValidationCDAResDTO() {
		super();
	}

	public ValidationCDAResDTO(final LogTraceInfoDTO traceInfo, final String inWorkflowInstanceId,
			final String inWarning) {
		super(traceInfo);
		workflowInstanceId = inWorkflowInstanceId;
		warning = inWarning;
	}
	
}
