package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import lombok.Getter;

/**
 * Exception that handles active mocks to return a useful endpoint result.
 * 
 * @author Simone Lungarella
 */
public class MockEnabledException extends RuntimeException {

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
