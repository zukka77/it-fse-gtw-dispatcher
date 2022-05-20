/**
 * 
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.exceptions;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author AndreaPerquoti
 * 
 * Generic exception to handle server-side errors.
 */
@Getter
@Setter
@AllArgsConstructor
public class ServerResponseException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1062386344600523308L;

	private final String target;
	
	private final String message;
	
	private final HttpStatus status;
	
	private final int statusCode;
	
	private final String detail;
	
	public ServerResponseException(ServerResponseException e) {
		this.target = e.getTarget();
		this.message = e.getMessage();
		this.status = e.getStatus();
		this.statusCode = e.getStatusCode();
		this.detail = e.getDetail();
	}
}
