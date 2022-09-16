package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

@Getter
public enum RestExecutionResultEnum {

	OK("00", "Pubblicazione effettuata correttamente.", null,EventStatusEnum.SUCCESS),
	OK_FORCED("01","Pubblicazione effettuata con forzatura.", null,EventStatusEnum.SUCCESS),
	MINING_CDA_ERROR("/msg/cda-element", "Errore in fase di estrazione del CDA.", ErrorLogEnum.KO_PUB,EventStatusEnum.BLOCKING_ERROR), 
	SYNTAX_ERROR("/msg/syntax", "Errore di sintassi.", ErrorLogEnum.KO_PUB,EventStatusEnum.BLOCKING_ERROR),
	SEMANTIC_ERROR("/msg/semantic", "Errore semantico.", ErrorLogEnum.KO_PUB,EventStatusEnum.BLOCKING_ERROR),
	VOCABULARY_ERROR("/msg/vocabulary", "Errore vocabolario.", ErrorLogEnum.KO_PUB,EventStatusEnum.BLOCKING_ERROR),
	CDA_MATCH_ERROR("/msg/cda-match", "Errore in fase di recupero dell'esito della verifica.", ErrorLogEnum.KO_PUB,EventStatusEnum.BLOCKING_ERROR), 
	EMPTY_FILE_ERROR("/msg/empty-file", "File vuoto.", ErrorLogEnum.KO_INVALID_DATA,EventStatusEnum.BLOCKING_ERROR),
	DOCUMENT_TYPE_ERROR("/msg/document-type", "Il documento non è pdf.", ErrorLogEnum.KO_INVALID_DATA,EventStatusEnum.BLOCKING_ERROR),
	DOCUMENT_HASH_VALIDATION_ERROR("/msg/document-hash", "Verifica hash fallita.", ErrorLogEnum.KO_INVALID_DATA,EventStatusEnum.BLOCKING_ERROR),
	MANDATORY_ELEMENT_ERROR("/msg/mandatory-element", "Campo obbligatorio non presente.", ErrorLogEnum.KO_INVALID_DATA,EventStatusEnum.BLOCKING_ERROR),
	FORMAT_ELEMENT_ERROR("/msg/invalid-format", "Formato campo non valido.", ErrorLogEnum.KO_INVALID_DATA,EventStatusEnum.BLOCKING_ERROR),
	MANDATORY_ELEMENT_ERROR_TOKEN("/msg/mandatory-element-token", "Token JWT non valido.", ErrorLogEnum.KO_INVALID_DATA,EventStatusEnum.BLOCKING_ERROR),
	INVALID_TOKEN_FIELD("/msg/jwt-validation", "Campo token JWT non valido.", ErrorLogEnum.KO_INVALID_DATA,EventStatusEnum.BLOCKING_ERROR),
	FHIR_MAPPING_ERROR("/msg/fhir-mapping-type", "Mapping fhir fallito.", ErrorLogEnum.KO_FHIR,EventStatusEnum.BLOCKING_ERROR),
	FHIR_MAPPING_TIMEOUT("/msg/fhir-mapping-timeout", "Mapping fhir timeout.", ErrorLogEnum.KO_FHIR,EventStatusEnum.BLOCKING_ERROR),
	GENERIC_TIMEOUT("/msg/generic-timeout", "Generic timeout.", ErrorLogEnum.KO_TIMEOUT,EventStatusEnum.NON_BLOCKING_ERROR),
	WORKFLOW_ID_ERROR("/msg/workflow-id-error-extraction", "Errore in fase di estrazione del workflow id.", ErrorLogEnum.KO_INVALID_DATA,EventStatusEnum.BLOCKING_ERROR),
	RECORD_NOT_FOUND("msg/record-not-found", "Record non trovato.", ErrorLogEnum.KO_INVALID_DATA,EventStatusEnum.BLOCKING_ERROR),
	SERVER_ERROR("msg/server-error", "Errore nella comunicazione con il client", ErrorLogEnum.KO_GENERIC,EventStatusEnum.BLOCKING_ERROR),
	GENERIC_ERROR("/msg/generic-error", "Errore generico.", ErrorLogEnum.KO_GENERIC, EventStatusEnum.BLOCKING_ERROR);


	private String type;
	private String title;
	private ErrorLogEnum errorCategory;
	private EventStatusEnum eventStatusEnum;

	private RestExecutionResultEnum(String inType, String inTitle, ErrorLogEnum inErrorCategory, EventStatusEnum inEventStatusEnum) {
		type = inType;
		title = inTitle;
		errorCategory = inErrorCategory;
		eventStatusEnum = inEventStatusEnum;
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