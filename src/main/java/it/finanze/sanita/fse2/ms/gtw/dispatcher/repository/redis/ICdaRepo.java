package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.redis;

import java.io.Serializable;

public interface ICdaRepo extends Serializable {

	/**
	 * Inserts a new item in the repository.
	 * 
	 * @param hashedCDA The value to be inserted.
	 * @param wii      The key of the item represented by the transaction Id.
	 */
	void create(String hashedCDA, String wii);

	/**
	 * Returns the value of the key: {@code txID}.
	 * 
	 * @param txID The key to search.
	 * @return The value of the key: {@code txID}.
	 */
	String getItem(String txID);

}
