/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class IniReferenceResponseDTO extends ResponseDTO {

	private String uuid;
	
	private String documentType;

	private String errorMessage;

	public IniReferenceResponseDTO() {
		super();
	}
	
}

