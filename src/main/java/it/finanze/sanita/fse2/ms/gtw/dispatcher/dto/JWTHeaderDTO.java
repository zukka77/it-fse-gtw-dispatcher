package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import org.apache.commons.lang3.StringUtils;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JWTHeaderDTO {

	/**
	 * Algoritmo utilizzato per la firma del token. Valori ammessi: RS256, RS383, RS512.
	 */
	private String alg;

	/**
	 * Tipologia di token. DEVE essere valorizzato con il valore 'JWT'.
	 */
	private String typ;

	/**
	 * Un riferimento opzionale alla chiave usata per la firma del token. Anche se valorizzato non viene utilizzato nella fase di verifica.
	 */
	private String kid;

	/**
	 * certificato X.509 utilizzato per la firma del token. 
	 * Valore in formato DER, codificato in base64.
	 */
	private String x5c;

	/**
	 * Map the object from JSON to object.
	 * 
	 * @param json The JWT token raw to map.
	 * @return The object built or {@code null} if the JSON is invalid.
	 */
	public static JWTHeaderDTO extractHeader(String json) {
		
		JWTHeaderDTO jwtHeader = null;
		try {
			jwtHeader = StringUtility.fromJSON(json, JWTHeaderDTO.class); 
		} catch (Exception e) {
			jwtHeader = null;
			log.error("Error while validating JWT header");
		}
		
		return jwtHeader;
	}

	public static String validateHeader(JWTHeaderDTO header) {
		String error = null;
		
		if (header == null) {
			error = "JWT header is not valid";
		} else if (!Constants.App.JWT_TOKEN_TYPE.equals(header.getTyp())) {
			error = "Invalid JWT token type";
		} else if (!JWTAllowdALG.isAllowed(header.getAlg())) {
			error = "Invalid JWT token algorithm";
		} else if (StringUtils.isEmpty(header.getX5c())) {
			error = "JWT token certificate is mandatory";
		}

		return error;
	}
	
	private enum JWTAllowdALG {
		RS256, RS384, RS512;

		private static boolean isAllowed(final String value) {
			
			for (JWTAllowdALG v : JWTAllowdALG.values()) {
				if (v.name().equals(value)) {
					return true;
				}
			}
			return false;
		}
	}
	
}
