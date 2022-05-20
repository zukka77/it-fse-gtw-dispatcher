/**
 * 
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

/**
 * @author AndreaPerquoti
 *
 */
@Slf4j
public class JsonUtility {


	/**
	 * Private constructor to avoid instantiation.
	 * 
	 * @throws IllegalStateException
	 */
	private JsonUtility() {
//		throw new IllegalStateException("Questa è una classe di utilità non va istanziata!!!");
	}

	private static ObjectMapper mapper = new ObjectMapper(); 

	/**
	 * Methods that converts an Object to a JSON string.
	 * 
	 * @param oggetto Object to convert.
	 * @return JSON String representation of the Object.
	 */
	public static <T> String objectToJson(T obj) {
		String jsonString = "";

		try {
			jsonString = mapper.writeValueAsString(obj);
		} catch (Exception e) {
			log.error("Errore durante la conversione da oggetto {} a string json: {}", obj.getClass(), e);
		}

		return jsonString;
	}
	
	/**
	 * Methods that converts a JSON String to a Class of a defined type.
	 * 
	 * @param stringa JSON String representation of the Object.
	 * @return Object created from the JSON String or {@code null} if the conversion fails.
	 */
	public static <T> T jsonToObject(String jsonString, Class<T> clazz) {
		T obj = null;
		try {
			obj = mapper.readValue(jsonString, clazz);
		} catch (Exception e) {
			log.error("Errore durante la conversione da stringa json a oggetto: {}", e);
		}

		return obj;
	}
	
	/**
	 * Methods that converts a JSON String to a Node structure.
	 * 
	 * @param stringa JSON String representation of the Object.
	 * @return ObjectNode created from the JSON String or {@code null} if the conversion fails.
	 */
	public static ObjectNode getNodeFromJson(String jsonString) {
		ObjectNode obj = null;
		try {
			obj = mapper.readValue(jsonString, ObjectNode.class);
		} catch (Exception e) {
			log.error("Errore durante la conversione da stringa json a struttura a nodi: {}", e);
		}
		
		return obj;
	}

}
