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
public class ValidationCDAResDTO extends ResponseDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -2144353497297675698L;
	
	@Size(min = 0, max = 256)
	private String workflowInstanceId;

	public ValidationCDAResDTO() {
		super();
	}

	public ValidationCDAResDTO(final LogTraceInfoDTO traceInfo, final String inWorkflowInstanceId) {
		super(traceInfo);
		workflowInstanceId = inWorkflowInstanceId;
	}
	
}
