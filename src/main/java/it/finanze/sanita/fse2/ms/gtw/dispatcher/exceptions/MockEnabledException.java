/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import lombok.Getter;

/**
 * Exception that handles active mocks to return a useful endpoint result.
 */
public class MockEnabledException extends RuntimeException {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -6705543139433416907L;

	@Getter
    private String iniErrorMessage;

    @Getter
    private String edsErrorMessage;

    public MockEnabledException(final String inIniError, final String inEdsError) {
        super();
        iniErrorMessage = inIniError;
        edsErrorMessage = inEdsError;
    }

}
