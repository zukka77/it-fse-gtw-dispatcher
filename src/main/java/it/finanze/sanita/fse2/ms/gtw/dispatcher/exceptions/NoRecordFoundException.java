/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

/**
 * 
 *
 *	Eccezione di record not found.
 */
public class NoRecordFoundException extends RuntimeException {

	/**
	 * Seriale.
	 */
	private static final long serialVersionUID = 5632725723070077498L;

	/**
	 * Costruttore.
	 * 
	 * @param msg	messaggio
	 */
	public NoRecordFoundException(final String msg) {
		super(msg);
	}
	
	/**
	 * Costruttore.
	 * 
	 * @param msg	messaggio
	 * @param e		eccezione
	 */
	public NoRecordFoundException(final String msg, final Exception e) {
		super(msg, e);
	}
	
	/**
	 * Costruttore.
	 * 
	 * @param e	eccezione.
	 */
	public NoRecordFoundException(final Exception e) {
		super(e);
	}
	
}
