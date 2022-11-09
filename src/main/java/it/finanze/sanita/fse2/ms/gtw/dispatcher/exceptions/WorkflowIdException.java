/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) 
public class WorkflowIdException extends RuntimeException{

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 2321017082632850476L;

	public WorkflowIdException(String message) {
		super(message);
	}
	
}
