/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 *	DTO used to return validation result.
 */
@Getter
@Setter
public class ValidationResDTO extends ResponseDTO {

	@Size(min = 0, max = 256)
	@Schema(description = "Identificativo univoco della transazione")
	private String workflowInstanceId;
	
	@Size(min = 0, max = 10000)
	@Schema(description = "Dettaglio del warning")
	private String warning;

	public ValidationResDTO() {
		super();
	}

	public ValidationResDTO(final LogTraceInfoDTO traceInfo, final String inWorkflowInstanceId, final String inWarning) {
		super(traceInfo);
		workflowInstanceId = inWorkflowInstanceId;
		warning = inWarning;
	}
	
}
