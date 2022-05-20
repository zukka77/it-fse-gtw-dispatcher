package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum PublicationResultEnum {

	OK("00", "Pubblicazione effettuata correttamente."),
	OK_FORCED("01","Pubblicazione effettuata con forzatura."),
	SECURITY_ERROR("/msg/security", "Errore in fase di verifica della sicurezza"), 
	MINING_CDA_ERROR("/msg/cda-element", "Errore in fase di estrazione del CDA."), 
	SYNTAX_ERROR("/msg/syntax", "Errore di sintassi."),
	SEMANTIC_ERROR("/msg/semantic", "Errore semantico."),
	VOCABULARY_ERROR("/msg/vocabulary", "Errore vocabolario."),
	CDA_MATCH_ERROR("/msg/cda-element", "Errore in fase di recupero dell'esito della verifica."), 
	PUBLISHING_ERROR("/msg/publishing-error", "Errore semantico."),
	EMPTY_FILE_ERROR("/msg/empty-file", "File vuoto."),
	DOCUMENT_TYPE_ERROR("/msg/document-type", "Il documento non è pdf."),
	DOCUMENT_SIZE_ERROR("/msg/document-size", "Il documento risulta essere vuoto."),
	SIGNED_VALIDATION_ERROR("/msg/document-type", "Verifica della firma fallita."),
	MANDATORY_ELEMENT_ERROR("/msg/mandatory-element", "Campo obbligatorio non presente."),
	MANDATORY_ELEMENT_ERROR_TOKEN("/msg/mandatory-element", "Token jwt non presente."),
	FHIR_MAPPING_ERROR("/msg/fhir-mapping-type", "Mapping fhir fallito.");

	private String type;
	private String title;

	private PublicationResultEnum(String inType, String inTitle) {
		type = inType;
		title = inTitle;
	}

	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

}