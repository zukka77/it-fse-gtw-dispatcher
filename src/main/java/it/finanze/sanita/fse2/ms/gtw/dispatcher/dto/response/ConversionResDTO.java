/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import lombok.Getter;
import lombok.Setter;

/**
 *	DTO used to return validation result.
 */
@Getter
@Setter
public class ConversionResDTO extends ResponseDTO {

	@Size(min = 0, max = 256)
	@Schema(description = "Identificativo univoco della transazione")
	private String workflowInstanceId;
	
	@Size(min = 0, max = Constants.App.MAX_SIZE_WARNING)
	@Schema(description = "Dettaglio del warning")
	private String warning;

	@Size(min = 0)
	@Schema(description = "Bundle Fhir")
	private String fhirBundle;

	public ConversionResDTO() {
		super();
	}

	public ConversionResDTO(final LogTraceInfoDTO traceInfo, final String inWorkflowInstanceId, final String inWarning, final String inFhirBundle) {
		super(traceInfo);
		workflowInstanceId = inWorkflowInstanceId;
		warning = inWarning;
		fhirBundle=inFhirBundle;
	}
	
}
