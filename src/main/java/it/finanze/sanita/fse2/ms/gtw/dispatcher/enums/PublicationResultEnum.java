package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

@Getter
public enum PublicationResultEnum {

	OK("00", "Pubblicazione effettuata correttamente.", null),
	OK_FORCED("01","Pubblicazione effettuata con forzatura.", null),
	SECURITY_ERROR("/msg/security", "Errore in fase di verifica della sicurezza", ErrorLogEnum.KO_PUB), 
	MINING_CDA_ERROR("/msg/cda-element", "Errore in fase di estrazione del CDA.", ErrorLogEnum.KO_PUB), 
	SYNTAX_ERROR("/msg/syntax", "Errore di sintassi.", ErrorLogEnum.KO_PUB),
	SEMANTIC_ERROR("/msg/semantic", "Errore semantico.", ErrorLogEnum.KO_PUB),
	VOCABULARY_ERROR("/msg/vocabulary", "Errore vocabolario.", ErrorLogEnum.KO_PUB),
	CDA_MATCH_ERROR("/msg/cda-element", "Errore in fase di recupero dell'esito della verifica.", ErrorLogEnum.KO_PUB), 
	PUBLISHING_ERROR("/msg/publishing-error", "Errore semantico.", ErrorLogEnum.KO_PUB),
	EMPTY_FILE_ERROR("/msg/empty-file", "File vuoto.", ErrorLogEnum.KO_INVALID_DATA),
	DOCUMENT_TYPE_ERROR("/msg/document-type", "Il documento non è pdf.", ErrorLogEnum.KO_INVALID_DATA),
	DOCUMENT_SIZE_ERROR("/msg/document-size", "Il documento risulta essere vuoto.", ErrorLogEnum.KO_INVALID_DATA),
	DOCUMENT_HASH_VALIDATION_ERROR("/msg/document-hash", "Verifica hash fallita.", ErrorLogEnum.KO_INVALID_DATA),
	MANDATORY_ELEMENT_ERROR("/msg/mandatory-element", "Campo obbligatorio non presente.", ErrorLogEnum.KO_INVALID_DATA),
	FORMAT_ELEMENT_ERROR("/msg/invalid-format", "Formato campo non valido.", ErrorLogEnum.KO_INVALID_DATA),
	MANDATORY_ELEMENT_ERROR_TOKEN("/msg/mandatory-element", "Token JWT non valido.", ErrorLogEnum.KO_INVALID_DATA),
	INVALID_TOKEN_FIELD("/msg/jwt-validation", "Campo token JWT non valido.", ErrorLogEnum.KO_INVALID_DATA),
	FHIR_MAPPING_ERROR("/msg/fhir-mapping-type", "Mapping fhir fallito.", ErrorLogEnum.KO_PUB);

	private String type;
	private String title;
	private ErrorLogEnum errorCategory;

	private PublicationResultEnum(String inType, String inTitle, ErrorLogEnum inErrorCategory) {
		type = inType;
		title = inTitle;
		errorCategory = inErrorCategory;
	}

}