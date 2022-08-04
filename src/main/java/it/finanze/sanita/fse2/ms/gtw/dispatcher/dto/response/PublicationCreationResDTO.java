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
public class PublicationCreationResDTO extends ResponseDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -1550024371939901939L;
	
	@Size(min = 0, max = 10000)
	@Schema(description = "Dettaglio del warning")
	private String warning;
	
	@Size(min = 0, max = 256)
	@Schema(description = "Identificativo del workflow instance id")
	private String workflowInstanceId;

	public PublicationCreationResDTO() {
		super();
	}

	public PublicationCreationResDTO(final LogTraceInfoDTO traceInfo, String inWarning,final String inWorkflowInstanceId) {
		super(traceInfo);
		warning = inWarning;
		workflowInstanceId = inWorkflowInstanceId;
	}
	
}
