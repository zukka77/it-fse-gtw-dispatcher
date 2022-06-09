package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

@Getter
public enum ValidationResultEnum {

	OK("about:blank", "Ok", null), 
	MANDATORY_ELEMENT_ERROR("/msg/mandatory-element", "Campo obbligatorio non presente.", ErrorLogEnum.KO_INVALID_DATA), 
	EMPTY_FILE_ERROR("/msg/empty-file", "File vuoto.", ErrorLogEnum.KO_INVALID_DATA), 
	FILE_GENERIC_ERROR("/msg/file-generic", "Errore in fase di gestione del file.", ErrorLogEnum.KO_INVALID_DATA), 
	DOCUMENT_TYPE_ERROR("/msg/document-type", "Il documento non è pdf.", ErrorLogEnum.KO_INVALID_DATA), 
	MINING_CDA_ERROR("/msg/mining-cda", "Errore in fase di estrazione del CDA.", ErrorLogEnum.KO_VAL), 
	SYNTAX_ERROR("/msg/syntax", "Errore di sintassi.", ErrorLogEnum.KO_VAL),
	SEMANTIC_ERROR("/msg/semantic", "Errore semantico.", ErrorLogEnum.KO_VAL),
	VOCABULARY_ERROR("/msg/vocabulary", "Errore vocabolario.", ErrorLogEnum.KO_VAL),
	GENERIC_ERROR("/msg/generic", "Errore generico.", ErrorLogEnum.KO_VAL),
	MANDATORY_ELEMENT_ERROR_TOKEN("/msg/mandatory-element", "Token jwt non presente.", ErrorLogEnum.KO_INVALID_DATA),
	FORMAT_ELEMENT_ERROR("/msg/invalid-format", "Formato campo non valido.", ErrorLogEnum.KO_INVALID_DATA),
	INVALID_TOKEN_FIELD("msg/jwt-validation", "Campo token JWT non valido", ErrorLogEnum.KO_INVALID_DATA),
	WORKFLOW_ID_ERROR("/msg/workflow-id-error-extraction", "Errore in fase di estrazione del workflow id.", ErrorLogEnum.KO_VAL);

	private String type;
	private String title;
	private ErrorLogEnum errorCategory;

	private ValidationResultEnum(String inType, String inTitle, ErrorLogEnum inErrorCategory) {
		type = inType;
		title = inTitle;
		errorCategory = inErrorCategory;
	}
	
	public static ValidationResultEnum get(String inType) {
		ValidationResultEnum out = null;
		for (ValidationResultEnum v:ValidationResultEnum.values()) {
			if (v.getType().equalsIgnoreCase(inType)) {
				out = v;
				break;
			}
		}
		return out;
	}


}