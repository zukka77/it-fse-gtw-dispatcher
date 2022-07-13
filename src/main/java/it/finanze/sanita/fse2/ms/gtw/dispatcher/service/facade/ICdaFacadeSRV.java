package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.facade;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;

/**
 * @author vincenzoingenito
 *
 *         Cda facade interface service.
 */
public interface ICdaFacadeSRV extends Serializable {

	/**
	 * Inserts a new item in the repository.
	 * 
	 * @param hashedCDA The value to be inserted.
	 * @param wii      The key of the item represented by the transaction Id.
	 */
	void create(String wii, String hashedCDA);

	/**
	 * Returns the value of the key: {@code hash}.
	 * 
	 * @param hash The key to search.
	 * @return The value of the key: {@code hash}.
	 */
	String get(String hash);

	/**
	 * Check presence of the hash on Redis.
	 * 
	 * @param hashToValidate The hash to validate.
	 * @param wii The workflowInstanceId to use to validate.
	 * @return The workflowInstanceId saved in validation time or {@code null} if never validated.
	 */
	ValidationDataDTO retrieveValidationInfo(String hashToValidate, String wii);
}
