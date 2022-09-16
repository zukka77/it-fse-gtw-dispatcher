package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum ErrorLogEnum implements ILogEnum {

	KO_GENERIC("KO-GENERIC", "Errore generico"),
	KO_TIMEOUT("KO-TIMEOUT", "Errore timeout"),
	KO_VAL("KO-VAL", "Errore nella validazione del CDA"),
	KO_PUB("KO-PUB", "Errore nella pubblicazione del CDA"),
	KO_INVALID_DATA("KO-INV-DATA", "Errore: dati di input non validi"),
	KO_SIGN_PDF("KO-SIGN_PDF", "Errore nella firma del PDF"),
	KO_FHIR("KO-FHIR", "Errore nella chiamata al gtw-fhir-mapping"),
	KO_KAFKA("KO-IND", "Errore nella chiamata a Kafka"),
	KO_MONGO_DB("KO-MONGO-DB", "Errore nella chiamata a MongoDB"),
	KO_MONGO_DB_NOT_FOUND("KO-MONGO-DB-NOT-FOUND", "Elemento non trovato sul MongoDB"),
	KO_REDIS("KO-REDIS", "Errore nella chiamata a Redis"),
	KO_REDIS_NOT_FOUND("KO-REDIS-NOT-FOUND", "Elemento non trovato in cache"); 

	private String code;
	
	public String getCode() {
		return code;
	}

	private String description;

	private ErrorLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public String getDescription() {
		return description;
	}

}

