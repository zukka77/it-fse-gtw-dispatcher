/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.facade;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;

/**
 *   Cda facade interface service.
 */
public interface ICdaFacadeSRV extends Serializable {

	/**
	 * Inserts a new item in the repository.
	 * 
	 * @param hashedCDA The value to be inserted.
	 * @param wii      The key of the item represented by the transaction Id.
	 * @param objectID The primary key of the item
	 */
	void create(String hashedCDA, String wii, String transformID, String engineID);

	/**
	 * Returns the value of the key: {@code hash}.
	 * 
	 * @param hash The key to search.
	 * @return The value of the key: {@code hash}.
	 */
	String get(String hash);

	ValidationDataDTO getByWorkflowInstanceId(String wid);

	/**
	 * Check presence of the hash on Mongo for the transaction Id.
	 * 
	 * @param hashToValidate The hash to validate.
	 * @param txID The key to use to search hash.
	 * @return The workflowInstanceId saved in validation time or {@code null} if never validated.
	 */
	ValidationDataDTO retrieveValidationInfo(String hashToValidate, String txID);

	/**
	 * Deletes a record on Mongo identified by its {@code hashToConsume}.
	 * 
	 * @param hashToConsume The record key to delete.
	 * @return {@code true} if deleted, {@code false} otherwise.
	 */
	boolean consumeHash(String hashToConsume);
}
