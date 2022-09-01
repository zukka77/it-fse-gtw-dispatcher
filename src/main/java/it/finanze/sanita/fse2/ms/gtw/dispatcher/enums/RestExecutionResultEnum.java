package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

@Getter
public enum RestExecutionResultEnum {

	OK("00", "Pubblicazione effettuata correttamente.", null),
	OK_FORCED("01","Pubblicazione effettuata con forzatura.", null),
	MINING_CDA_ERROR("/msg/cda-element", "Errore in fase di estrazione del CDA.", ErrorLogEnum.KO_PUB), 
	SYNTAX_ERROR("/msg/syntax", "Errore di sintassi.", ErrorLogEnum.KO_PUB),
	SEMANTIC_ERROR("/msg/semantic", "Errore semantico.", ErrorLogEnum.KO_PUB),
	VOCABULARY_ERROR("/msg/vocabulary", "Errore vocabolario.", ErrorLogEnum.KO_PUB),
	CDA_MATCH_ERROR("/msg/cda-match", "Errore in fase di recupero dell'esito della verifica.", ErrorLogEnum.KO_PUB), 
	EMPTY_FILE_ERROR("/msg/empty-file", "File vuoto.", ErrorLogEnum.KO_INVALID_DATA),
	DOCUMENT_TYPE_ERROR("/msg/document-type", "Il documento non è pdf.", ErrorLogEnum.KO_INVALID_DATA),
	DOCUMENT_HASH_VALIDATION_ERROR("/msg/document-hash", "Verifica hash fallita.", ErrorLogEnum.KO_INVALID_DATA),
	MANDATORY_ELEMENT_ERROR("/msg/mandatory-element", "Campo obbligatorio non presente.", ErrorLogEnum.KO_INVALID_DATA),
	FORMAT_ELEMENT_ERROR("/msg/invalid-format", "Formato campo non valido.", ErrorLogEnum.KO_INVALID_DATA),
	MANDATORY_ELEMENT_ERROR_TOKEN("/msg/mandatory-element-token", "Token JWT non valido.", ErrorLogEnum.KO_INVALID_DATA),
	INVALID_TOKEN_FIELD("/msg/jwt-validation", "Campo token JWT non valido.", ErrorLogEnum.KO_INVALID_DATA),
	FHIR_MAPPING_ERROR("/msg/fhir-mapping-type", "Mapping fhir fallito.", ErrorLogEnum.KO_FHIR),
	FHIR_MAPPING_TIMEOUT("/msg/fhir-mapping-timeout", "Mapping fhir timeout.", ErrorLogEnum.KO_FHIR),
	WORKFLOW_ID_ERROR("/msg/workflow-id-error-extraction", "Errore in fase di estrazione del workflow id.", ErrorLogEnum.KO_INVALID_DATA),
	RECORD_NOT_FOUND("msg/record-not-found", "Record non trovato.", ErrorLogEnum.KO_INVALID_DATA),
	SERVER_ERROR("msg/server-error", "Errore nella comunicazione con il client", ErrorLogEnum.KO_GENERIC),
	GENERIC_ERROR("/msg/generic-error", "Errore generico.", ErrorLogEnum.KO_GENERIC);


	private String type;
	private String title;
	private ErrorLogEnum errorCategory;

	private RestExecutionResultEnum(String inType, String inTitle, ErrorLogEnum inErrorCategory) {
		type = inType;
		title = inTitle;
		errorCategory = inErrorCategory;
	}

	public static RestExecutionResultEnum get(String inType) {
		RestExecutionResultEnum out = null;
		for (RestExecutionResultEnum v:RestExecutionResultEnum.values()) {
			if (v.getType().equalsIgnoreCase(inType)) {
				out = v;
				break;
			}
		}
		return out;
	}

	public static RestExecutionResultEnum fromRawResult(RawValidationEnum rawResult) {

		RestExecutionResultEnum result;

		switch (rawResult) {
			case VOCABULARY_ERROR:
				result = RestExecutionResultEnum.VOCABULARY_ERROR;
				break;
			case SEMANTIC_ERROR:
				result = RestExecutionResultEnum.SEMANTIC_ERROR;
				break;
			case SYNTAX_ERROR:
				result = RestExecutionResultEnum.SYNTAX_ERROR;
				break;
			case OK:
				result = RestExecutionResultEnum.OK;
				break;
			default:
				result = RestExecutionResultEnum.GENERIC_ERROR;
				break;
		}

		return result;
	}

}