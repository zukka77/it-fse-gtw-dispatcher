package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import lombok.Getter;

public class ValidationPublicationErrorException  extends RuntimeException {

	/**
	 * Serial.
	 */
	private static final long serialVersionUID = 1L;
	
	@Getter
	private final RestExecutionResultEnum result;

	@Getter
	private String errorInstance;

	public ValidationPublicationErrorException(final RestExecutionResultEnum inResult, final String msg, final String inErrorInstance) {
		super(msg);
		result = inResult;
		errorInstance = inErrorInstance;
	}

}