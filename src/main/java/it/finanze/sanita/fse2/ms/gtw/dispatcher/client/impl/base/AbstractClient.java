/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.base;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.exceptions.ServerResponseException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Abstract class for client implementations.
 */
public abstract class AbstractClient {

	protected void toServerResponseEx(String identifier, RestClientResponseException ex, String endpoint) {
		throw new ServerResponseException(
			endpoint,
			String.format("%s - Errore durante invocazione /%s", identifier, endpoint),
			HttpStatus.valueOf(ex.getRawStatusCode()),
			ex.getRawStatusCode(),
			ex.getResponseBodyAsString()
		);

	}

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
