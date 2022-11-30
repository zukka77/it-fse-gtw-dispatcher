/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import lombok.Getter;

public class NoRecordFoundException extends RuntimeException {

	@Getter
	private ErrorResponseDTO error;
	
	@Getter
	private Integer status;
	
	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -8624582745572789610L;

	public NoRecordFoundException(String msg) {
		super(msg);
	}

	public NoRecordFoundException(Exception e) {
		super(e);
	}
	
	public NoRecordFoundException(ErrorResponseDTO inError, Integer inStatus) {
		error = inError;
		status = inStatus;
	}

	public NoRecordFoundException(String msg, Exception e) {
		super(msg, e);
	}

}
