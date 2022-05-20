package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum ValidationResultEnum {

	OK("about:blank", "Ok"), 
	MANDATORY_ELEMENT_ERROR("/msg/mandatory-element", "Campo obbligatorio non presente."), 
	EMPTY_FILE_ERROR("/msg/empty-file", "File vuoto."), 
	FILE_GENERIC_ERROR("/msg/file-generic", "Errore in fase di gestione del file."), 
	DOCUMENT_TYPE_ERROR("/msg/document-type", "Il documento non è pdf."), 
	MINING_CDA_ERROR("/msg/mining-cda", "Errore in fase di estrazione del CDA."), 
	SYNTAX_ERROR("/msg/syntax", "Errore di sintassi."),
	SEMANTIC_ERROR("/msg/semantic", "Errore semantico."),
	VOCABULARY_ERROR("/msg/vocabulary", "Errore vocabolario."),
	GENERIC_ERROR("/msg/generic", "Errore generico."),
	MANDATORY_ELEMENT_ERROR_TOKEN("/msg/mandatory-element", "Token jwt non presente.");

	private String type;
	private String title;

	private ValidationResultEnum(String inType, String inTitle) {
		type = inType;
		title = inTitle;
	}

	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
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