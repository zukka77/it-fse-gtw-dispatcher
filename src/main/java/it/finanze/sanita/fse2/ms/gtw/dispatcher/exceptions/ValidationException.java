/**
 * 
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

/**
 * @author CPIERASC
 * 
 * Validation exeception.
 *
 */
public class ValidationException extends RuntimeException {

	/**
	 * Message constructor.
	 * 
	 * @param msg	Message to be shown.
	 */
	public ValidationException(final String msg) {
		super(msg);
	}
	
	/**
	 * Complete constructor.
	 * 
	 * @param msg	Message to be shown.
	 * @param e		Exception to be shown.
	 */
	public ValidationException(final String msg, final Exception e) {
		super(msg, e);
	}
	
	/**
	 * Exception constructor.
	 * 
	 * @param e	Exception to be shown.
	 */
	public ValidationException(final Exception e) {
		super(e);
	}
	
}
