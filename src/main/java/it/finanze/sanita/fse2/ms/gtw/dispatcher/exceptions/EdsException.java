/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import lombok.Getter;

public class EdsException extends RuntimeException {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 6592463845560227769L;
	
	@Getter
    private String errorMessage;

    public EdsException(final String inErrorMessage) {
        super();
        errorMessage = inErrorMessage;
    }

}
