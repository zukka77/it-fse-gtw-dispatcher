/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * The Class ValidationErrorResponseDTO.
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class ValidationErrorResponseDTO extends ErrorResponseDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -6684870619268235535L;
	
	@Schema(description = "Identificativo del workflow")
	@Size(min = 0, max = 256)
	private String workflowInstanceId;
	
	public ValidationErrorResponseDTO(final LogTraceInfoDTO traceInfo, final String inType, final String inTitle, final String inDetail, final Integer inStatus, final String inInstance, final String inWorkflowInstanceId) {
		super(traceInfo, inType, inTitle, inDetail, inStatus, inInstance);
		workflowInstanceId = inWorkflowInstanceId;
	}

}
