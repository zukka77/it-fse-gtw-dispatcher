/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.GetMergedMetadatiDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetMergedMetadatiResponseDTO extends ResponseDTO {

	private GetMergedMetadatiDTO response;

	public GetMergedMetadatiResponseDTO() {
		super();
	}

	public GetMergedMetadatiResponseDTO(final LogTraceInfoDTO traceInfo, final GetMergedMetadatiDTO inResponse) {
		super(traceInfo);
		response = inResponse;
	}

}

