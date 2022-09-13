package it.finanze.sanita.fse2.ms.gtw.dispatcher.utility;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;

import org.apache.commons.codec.binary.Hex;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.UIDModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class StringUtility {

	/**
	 * Private constructor to avoid instantiation.
	 */
	private StringUtility() {
		// Constructor intentionally empty.
	}

	/**
	 * Returns {@code true} if the String passed as parameter is null or empty.
	 * 
	 * @param str	String to validate.
	 * @return		{@code true} if the String passed as parameter is null or empty.
	 */
	public static boolean isNullOrEmpty(final String str) {
	    boolean out = false;
		if (str == null || str.isEmpty()) {
			out = true;
		}
		return out;
	}

	/**
	 * Returns the encoded String of the SHA-256 algorithm represented in base 64.
	 * 
	 * @param objectToEncode String to encode.
	 * @return String Encoded.
	 */
	public static String encodeSHA256(byte[] objectToEncode) {
		try {
		    final MessageDigest digest = MessageDigest.getInstance("SHA-256");
		    return Hex.encodeHexString(digest.digest(objectToEncode));
		} catch (Exception e) {
			log.error("Errore in fase di calcolo sha", e);
			throw new BusinessException("Errore in fase di calcolo SHA-256", e);
		}
	}
	
	/**
	 * Returns the encoded String of the SHA-256 algorithm represented in base 64.
	 * 
	 * @param objectToEncode String to encode.
	 * @return String Encoded.
	 */
	public static String encodeSHA256B64(String objectToEncode) {
		try {
		    final MessageDigest digest = MessageDigest.getInstance("SHA-256");
		    final byte[] hash = digest.digest(objectToEncode.getBytes());
		    return encodeBase64(hash);
		} catch (Exception e) {
			log.error("Errore in fase di calcolo sha", e);
			throw new BusinessException("Errore in fase di calcolo SHA-256", e);
		}
	}

	public static String decodeFromBase64(String encodedString) {
		return new String(Base64.getDecoder().decode(encodedString), StandardCharsets.UTF_8);
	}
	
	/**
	 * Returns the encoded String of the SHA-256 algorithm encoded represented in base hex.
	 * 
	 * @param objectToEncode String to encode.
	 * @return String Encoded.
	 */
	public static String encodeSHA256Hex(String objectToEncode) {
		try {
		    final MessageDigest digest = MessageDigest.getInstance("SHA-256");
		    final byte[] hash = digest.digest(objectToEncode.getBytes());
		    return encodeHex(hash);
		} catch (Exception e) {
			log.error("Errore in fase di calcolo sha", e);
			throw new BusinessException("Errore in fase di calcolo SHA-256", e);
		}
	}

	/**
	 * Encode in Base64 the byte array passed as parameter.
	 * 
	 * @param input	The byte array to encode.
	 * @return		The encoded byte array to String.
	 */
	public static String encodeBase64(final byte[] input) {
		return Base64.getEncoder().encodeToString(input);
	}

	/**
	 * Encodes the byte array passed as parameter in hexadecimal.
	 * 
	 * @param input	The byte array to encode.
	 * @return		The encoded byte array to String.
	 */
	public static String encodeHex(final byte[] input) {
		return Hex.encodeHexString(input);
	}

	public static String generateUUID() {
	    return UUID.randomUUID().toString();
	}

	public static String generateTransactionUID(final UIDModeEnum mode) {
	    
		String uid = null;

		if (!Arrays.asList(UIDModeEnum.values()).contains(mode)) {
			uid = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
		} else {
			switch (mode) {
				case HOSTNAME_UUID:
					try {
						InetAddress ip = InetAddress.getLocalHost();
						uid = ip.getHostName() + UUID.randomUUID().toString().replace("-", "");
					} catch (Exception e) {
						log.error("Error while retrieving host informations", e);
						throw new BusinessException("Error while retrieving host informations", e);
					}
					break;
				case IP_UUID:
					try {
						InetAddress ip = InetAddress.getLocalHost();
						uid = ip.toString().replace(ip.getHostName() + "/", "")
								+ UUID.randomUUID().toString().replace("-", "");
					} catch (Exception e) {
						log.error("Error while retrieving host informations", e);
						throw new BusinessException("Error while retrieving host informations", e);
					}
					break;
				case UUID_UUID:
					uid = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
					break;
			}
		}

		return uid;
	}
	
	public static String sanitizeMessage(final String message) {
		return message.replace("<script>", "").replace("</script>", "");
	}

	/**
	 * Transformation from Json to Object.
	 * 
	 * @param <T>	Generic type of return
	 * @param json	json
	 * @param cls	Object class to return
	 * @return		object
	 */
	public static <T> T fromJSON(final String json, final Class<T> cls) {
		return new Gson().fromJson(json, cls);
	}

	/**
	 * Transformation from Object to Json.
	 * 
	 * @param obj	object to transform
	 * @return		json
	 */
	public static String toJSON(final Object obj) {
		return new Gson().toJson(obj);
	}
	
	/**
	 * Transformation from Object to Json.
	 * 
	 * @param obj	object to transform
	 * @return		json
	 */
	public static String toJSONJackson(final Object obj) {
		String out = "";
		try {
			ObjectMapper objectMapper = new ObjectMapper(); 
			objectMapper.registerModule(new JavaTimeModule());
			objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			objectMapper.setSerializationInclusion(Include.NON_NULL);
			out = objectMapper.writeValueAsString(obj);
		} catch(Exception ex) {
			log.error("Error while running to json jackson");
			throw new BusinessException(ex);
		}
		return out; 
	}

	/**
	 * Metodo che permette data l'uri definita nelle prop di avere il nome del db
	 * 
	 * @param uri
	 * @return string
	 */
	public static String getDatabaseName(final String uri) { 
		int indexDBName = uri.lastIndexOf("/");
		String nameWithReplica = uri.substring(indexDBName+1, uri.length()).trim();
		if(nameWithReplica.contains("?")) {
			nameWithReplica = nameWithReplica.substring(0, nameWithReplica.indexOf('?')).trim();
		}
		return nameWithReplica;
	}
}
