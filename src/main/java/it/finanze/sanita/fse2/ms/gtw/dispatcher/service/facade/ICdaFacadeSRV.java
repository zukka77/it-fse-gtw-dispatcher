package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.facade;

import java.io.Serializable;

/**
 * @author vincenzoingenito
 *
 *         Cda facade interface service.
 */
public interface ICdaFacadeSRV extends Serializable {

	/**
	 * Inserts a new item in the repository.
	 * 
	 * @param txID      The key of the item represented by the transaction Id.
	 * @param hashedCDA The value to be inserted.
	 */
	void create(String txID, String hashedCDA);

	/**
	 * Returns the value of the key: {@code txID}.
	 * 
	 * @param txID The key to search.
	 * @return The value of the key: {@code txID}.
	 */
	String get(String txID);

	/**
	 * Check presence of the hash on Redis for the transaction Id.
	 * 
	 * @param hashToValidate The hash to validate.
	 * @param txID The key to use to search hash.
	 * @return {@code true} if hash is present and matches {@code hashToValidate}, {@code false} otherwise.
	 */
	boolean validateHash(String hashToValidate, String txID);

}
