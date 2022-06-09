package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum OperationLogEnum implements ILogEnum {

	VAL_CDA2("VAL-CDA2", "Validazione CDA2"),
	PUB_CDA2("PUB-CDA2", "Pubblicazione CDA2"),
	TRAS_CDA2("TRAS-CDA2", "Trasmissione CDA2"),
	TRAS_KAFKA("TRAS-KAFKA", "Invio topic su Kafka"),
	REDIS("REDIS", "Salvataggio/Query su Redis"); 

	private String code;
	
	public String getCode() {
		return code;
	}

	private String description;

	private OperationLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public String getDescription() {
		return description;
	}

}

