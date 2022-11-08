/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import lombok.Getter;
import lombok.Setter;

/**
 *	DTO used to return TS Document validation result.
 */
@Getter
@Setter
public class TSPublicationCreationResDTO extends ResponseDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -1550774371939901939L;

	private String workflowInstanceId;
	
	private String warning;
	
	public TSPublicationCreationResDTO() {
		super();
	}

	public TSPublicationCreationResDTO(final LogTraceInfoDTO traceInfo, final String inWorkflowInstanceId, final String inWarning) {
		super(traceInfo);
		workflowInstanceId = inWorkflowInstanceId;
		warning = inWarning;
	}
	
}
