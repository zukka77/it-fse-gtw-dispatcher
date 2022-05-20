/**
 * 
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ValidationResultEnum;

/**
 * @author CPIERASC
 * 
 * Validation error exception.
 *
 */
public class ValidationErrorException extends RuntimeException {

	/**
	 * Serial.
	 */
	private static final long serialVersionUID = 1L;
	
	private final ValidationResultEnum result;

	public ValidationErrorException(final ValidationResultEnum inResult, final String msg) {
		super(msg);
		result = inResult;
	}

	public ValidationResultEnum getResult() {
		return result;
	}

}
