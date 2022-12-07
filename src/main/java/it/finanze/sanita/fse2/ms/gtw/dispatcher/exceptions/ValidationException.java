/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import lombok.Getter;

/**
 * Validation exeception.
 */
public class ValidationException extends RuntimeException {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 1253566999469948023L;
	
	@Getter
	private ErrorResponseDTO error;

	public ValidationException(final ErrorResponseDTO inError) {
		error = inError;
	}
	
}
