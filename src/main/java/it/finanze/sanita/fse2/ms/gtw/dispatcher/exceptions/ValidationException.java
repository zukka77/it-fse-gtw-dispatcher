/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import lombok.Getter;

/**
 * @author CPIERASC
 * 
 * Validation exeception.
 *
 */
public class ValidationException extends RuntimeException {

	@Getter
	private ErrorResponseDTO error;
	/**
	 * Message constructor.
	 * 
	 * @param msg	Message to be shown.
	 */
	public ValidationException(final String msg) {
		super(msg);
	}

	public ValidationException(final ErrorResponseDTO inError) {
		error = inError;
	}
	
	/**
	 * Complete constructor.
	 * 
	 * @param msg	Message to be shown.
	 * @param e		Exception to be shown.
	 */
	public ValidationException(final String msg, final Exception e) {
		super(msg, e);
	}
	
	/**
	 * Exception constructor.
	 * 
	 * @param e	Exception to be shown.
	 */
	public ValidationException(final Exception e) {
		super(e);
	}
	
}
