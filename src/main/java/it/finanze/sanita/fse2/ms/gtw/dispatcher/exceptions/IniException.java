/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import lombok.Getter;

public class IniException extends RuntimeException {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -7571614465871671680L;
	
	@Getter
    private String errorMessage;

    public IniException(final String inErrorMessage) {
        super(inErrorMessage);
        errorMessage = inErrorMessage;
    }

}
