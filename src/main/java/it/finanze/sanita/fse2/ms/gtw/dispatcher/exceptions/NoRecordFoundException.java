/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import lombok.Getter;

public class NoRecordFoundException extends RuntimeException {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -1545283262352643111L;

	@Getter
	private ErrorResponseDTO error;
	
	@Getter
	private Integer status;
	
	public NoRecordFoundException(ErrorResponseDTO inError, Integer inStatus) {
		error = inError;
		status = inStatus;
	}

}
