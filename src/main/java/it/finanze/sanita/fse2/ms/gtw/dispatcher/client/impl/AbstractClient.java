/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import java.io.Serializable;

import org.springframework.web.client.HttpStatusCodeException;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.exceptions.ServerResponseException;

/**
 * Abstract class for client implementations.
 * 
 * @author CPIERASC
 */
public abstract class AbstractClient implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
	 * Error handler.
	 *
	 * @param e1 The thrown Exception.
	 * @param endpoint The endpoint that caused the error.
	 */
	protected void errorHandler(HttpStatusCodeException e1, String endpoint) {
		String msg = "Errore durante l'invocazione dell' API " + endpoint + ". Il sistema ha restituito un " + e1.getStatusCode();
		throw new ServerResponseException(endpoint, msg, e1.getStatusCode(), e1.getRawStatusCode(), e1.getLocalizedMessage());
	}

}
