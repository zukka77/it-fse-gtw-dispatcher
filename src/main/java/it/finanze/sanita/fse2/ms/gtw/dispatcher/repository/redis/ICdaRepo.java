/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.redis;

import java.io.Serializable;

public interface ICdaRepo extends Serializable {

	/**
	 * Inserts a new item in the repository.
	 * 
	 * @param hashedCDA The key of the item represented by the hash of CDA.
	 * @param wii      	The value to be inserted.
	 */
	void create(String hashedCDA, String wii);

	/**
	 * Returns the value of the key: {@code hash}.
	 * 
	 * @param hash The key to search.
	 * @return The value of the key: {@code hash}.
	 */
	String getItem(String hash);

	/**
	 * Deletes a record on Redis identified by its {@code txId}.
	 * 
	 * @param txID The workflow ID.
	 * @return {@code true} if deleted, {@code false} otherwise.
	 */
	Boolean delete(String txID);
}
