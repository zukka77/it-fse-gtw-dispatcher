/**
 * 
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
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
	private final RestExecutionResultEnum result;

	@Getter
	private final String workflowInstanceId;

	@Getter
	private final String errorInstance;

	public ValidationErrorException(final RestExecutionResultEnum inResult, final String msg, final String inWorkflowInstanceId, final String inErrorInstance) {
		super(msg);
		workflowInstanceId = inWorkflowInstanceId;
		result = inResult;
		errorInstance = inErrorInstance;
	}

}
