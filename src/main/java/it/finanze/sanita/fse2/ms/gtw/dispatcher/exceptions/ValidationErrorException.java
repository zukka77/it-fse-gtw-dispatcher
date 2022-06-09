/**
 * 
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ValidationResultEnum;
import lombok.Getter;

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
	
	@Getter
	private final ValidationResultEnum result;

	@Getter
	private final String workflowInstanceId;

	public ValidationErrorException(final ValidationResultEnum inResult, final String msg, final String inWorkflowInstanceId) {
		super(msg);
		workflowInstanceId = inWorkflowInstanceId;
		result = inResult;
	}

}
