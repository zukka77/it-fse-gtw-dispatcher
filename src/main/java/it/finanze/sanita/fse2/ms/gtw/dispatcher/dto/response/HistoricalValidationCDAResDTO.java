package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 *
 *	DTO used to return Historical Document validation result.
 */
@Getter
@Setter
public class HistoricalValidationCDAResDTO extends ResponseDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -2144377497297675698L;
	
	@Size(min = 0, max = 100)
	private String workflowInstanceId;

	public HistoricalValidationCDAResDTO() {
		super();
	}

	public HistoricalValidationCDAResDTO(final LogTraceInfoDTO traceInfo, final String inWorkflowInstanceId) {
		super(traceInfo);
		workflowInstanceId = inWorkflowInstanceId;
	}
	
}
