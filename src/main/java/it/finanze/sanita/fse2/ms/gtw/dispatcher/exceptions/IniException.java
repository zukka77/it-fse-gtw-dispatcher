package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import lombok.Getter;

public class IniException extends RuntimeException {

    @Getter
    private String errorMessage;

    public IniException(final String inErrorMessage) {
        super(inErrorMessage);
        errorMessage = inErrorMessage;
    }

}
