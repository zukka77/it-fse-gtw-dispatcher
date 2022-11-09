/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import lombok.Data;

@Data
public class GetMergedMetadatiDTO {

	private String errorMessage;
	
	private String marshallResponse;
}
