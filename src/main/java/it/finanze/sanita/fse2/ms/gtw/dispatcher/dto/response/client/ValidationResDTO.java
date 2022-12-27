/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ResponseDTO;
import lombok.Getter;
import lombok.Setter;

/**
 *	DTO used to return validation result.
 */
@Getter
@Setter
public class ValidationResDTO extends ResponseDTO {

	private final ValidationInfoDTO result;
	
	public ValidationResDTO() {
		super();
		result = null;
	}

	public ValidationResDTO(final LogTraceInfoDTO traceInfo, final ValidationInfoDTO inResult) {
		super(traceInfo);
		result = inResult;
	}
	
}
