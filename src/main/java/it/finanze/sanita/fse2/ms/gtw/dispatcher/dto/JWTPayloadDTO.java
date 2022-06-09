package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import org.apache.commons.lang3.StringUtils;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CfUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JWTPayloadDTO {

	/**
	 * Identificativo dell’entità che ha generato il token.
	 * Valorizzato con l’identificativo della struttura utente.
	 * Codifica ISTAT della Azienda (ASL) concatenato alla codifica HSP.11 -
	 * HSP.11bis - STS.11 - RIA.11. Nel caso di nodo regionale valorizzato con il
	 * codice ISTAT della regione.
	 */
	private String iss;

	/**
	 * Numero intero (timestamp in secondi) che indica il momento in cui il token è
	 * stato generato, serve per conoscere l’età di un token.
	 */
	private int iat;

	/**
	 * Numero intero (timestamp in secondi) che indica fino a quando il token sarà
	 * valido.
	 */
	private int exp;

	/**
	 * Identificativo univoco del token, serve per prevenire la generazione
	 * accidentale di token uguali.
	 */
	private String jti;

	/**
	 * Indica il destinatario per cui è stato creato il token.
	 */
	private String aud;

	/**
	 * Codice Fiscale dell’utente che fa richiesta del servizio di interoperabilità.
	 * Formato codifica conforme alle specifiche IHE (ITI TF-3).
	 */
	private String sub;

	/**
	 * Identificativo del dominio dell’utente (vedi TABELLA ORGANIZZAZIONE).
	 */
	private String subject_organization_id;

	/**
	 * Identificativo della struttura utente.
	 * Codifica ISTAT della Azienda (ASL) concatenato alla codifica HSP.11 -
	 * HSP.11bis - STS.11 - RIA.11.
	 * Nel caso di accesso da parte della struttura coincide con l’issuer.
	 * Nel caso di ruolo APR assume il valore del codice ISTAT dell’Azienda (ASL)
	 */
	private String locality;

	/**
	 * Ruolo dell’utente che effettua la richiesta, vedi TABELLA RUOLO.
	 */
	private String subject_role;

	/**
	 * Codice Fiscale dell’assistito cui si riferisce la richiesta o del
	 * genitore/tutore che ha richiesto l’operazione
	 * Codice fiscale dell’assistito, del genitore o del tutore, codificato secondo
	 * il tipo di dato CX HL7 V2.5 (per come indicato alle specifiche IHE TF-3).
	 */
	private String person_id;

	/**
	 * Indica la presa in carico del paziente.
	 * Valore booleano.
	 */
	private Boolean patient_consent;

	/**
	 * Contesto operativo della richiesta
	 * Vedi TABELLA CONTESTO OPERATIVO.
	 */
	private String purpose_of_use;

	/**
	 * Tipo di documento da registrare
	 * Codifica LOINC nel formato ('code1^^coding-scheme1','code2^^coding-scheme2').
	 */
	private String resource_hl7_type;

	/**
	 * Descrive il tipo di attività: CREATE, READ, UPDATE, DELETE.
	 * Vedi TABELLA TIPO ATTIVITA’.
	 */
	private String action_id;

	private String attachment_hash;

	/**
	 * Map the object from JSON to object.
	 * 
	 * @param json The JWT token raw to map.
	 * @return The object built or {@code null} if the JSON is invalid.
	 */
	public static JWTPayloadDTO extractPayload(String json) {

		JWTPayloadDTO jwtPayload = null;
		try {
			jwtPayload = StringUtility.fromJSON(json, JWTPayloadDTO.class);
		} catch (Exception e) {
			jwtPayload = null;
			log.error("Error while validating JWT payload DTO");
		}

		return jwtPayload;
	}

	public static String validatePayload(JWTPayloadDTO payload) {
		String error = null;

		if (payload == null) {
			error = "JWT payload is not valid";
		} else if (!Constants.App.JWT_TOKEN_AUDIENCE.equals(payload.getAud())) {
			error = "Invalid audience";
		} else if (!CfUtility.isValidCf(payload.getSub())) {
			error = "Invalid subject fiscal code";
		} else if (!CfUtility.isValidCf(payload.getPerson_id())) {
			error = "Invalid person fiscal code";
		} else if (StringUtils.isEmpty(payload.getSubject_organization_id())) {
			error = "Invalid subject organization id";
		} else if (StringUtils.isEmpty(payload.getLocality())) {
			error = "Invalid locality";
		} else if (StringUtils.isEmpty(payload.getSubject_role())) {
			error = "Invalid subject role";
		} else if (StringUtils.isEmpty(payload.getPurpose_of_use())) {
			error = "Invalid purpose of use";
		} else if (!ActionEnum.isAllowed(payload.getAction_id())) {
			error = "Action not allowed";
		} else if (StringUtils.isEmpty(payload.getIss())) {
			error = "Invalid issuer";
		} else if (StringUtils.isEmpty(payload.getJti())) {
			error = "Invalid Jti";
		} else if (!payload.getSub().equals(payload.getPerson_id())
				&& payload.getPatient_consent() == null) {
			error = "Patient consent is mandatory if subject and person id are different";
		}

		return error;
	}

	private enum ActionEnum {

		CREATE, READ, UPDATE, DELETE;

		private static boolean isAllowed(final String value) {

			for (ActionEnum v : ActionEnum.values()) {
				if (v.name().equals(value)) {
					return true;
				}
			}
			return false;
		}
	}

}
