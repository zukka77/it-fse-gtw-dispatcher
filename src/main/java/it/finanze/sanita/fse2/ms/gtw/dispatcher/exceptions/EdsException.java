package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import lombok.Getter;

public class EdsException extends RuntimeException {

    @Getter
    private String errorMessage;

    public EdsException(final String inErrorMessage) {
        super();
        errorMessage = inErrorMessage;
    }

}
