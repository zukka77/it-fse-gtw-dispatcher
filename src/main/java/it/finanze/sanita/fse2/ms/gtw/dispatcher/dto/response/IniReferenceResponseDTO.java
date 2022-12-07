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

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 5457503502983726876L;

	private String uuid;

	private String errorMessage;

	public IniReferenceResponseDTO() {
		super();
	}
	
}

