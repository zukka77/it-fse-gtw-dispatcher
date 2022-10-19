/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.exceptions;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Riccardo Bonesi
 * 
 * Exception to handle record not found exception error received from clients.
 */
@Getter
@Setter
@AllArgsConstructor
public class RecordNotFoundException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1062382244600523308L;

	private String target;
	
	private String message;
	
	private HttpStatus status;
	
	private int statusCode;
	
	private String detail;
	
	public RecordNotFoundException(ServerResponseException e) {
		this.target = e.getTarget();
		this.message = e.getMessage();
		this.status = e.getStatus();
		this.statusCode = e.getStatusCode();
		this.detail = e.getDetail();
	}
	
	/**
	 * Message constructor.
	 * 
	 * @param msg	Message to be shown.
	 */
	public RecordNotFoundException(final String msg) {
		message = msg;
	}
}
