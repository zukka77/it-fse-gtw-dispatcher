package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PublicationResultEnum;

public class ValidationPublicationErrorException  extends RuntimeException {

	/**
	 * Serial.
	 */
	private static final long serialVersionUID = 1L;
	
	private final PublicationResultEnum result;

	public ValidationPublicationErrorException(final PublicationResultEnum inResult, final String msg) {
		super(msg);
		result = inResult;
	}

	public PublicationResultEnum getResult() {
		return result;
	}

}