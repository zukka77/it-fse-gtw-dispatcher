package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.redis;

import java.io.Serializable;

public interface ICdaRepo extends Serializable {

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
	String getItem(String txID);

}
