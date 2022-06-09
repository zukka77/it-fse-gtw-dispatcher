package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum ActivityEnum {

	VALIDATION("V"), 
	PRE_PUBLISHING("P"),
	HISTORICAL_DOC_VALIDATION("HP"),
	HISTORICAL_DOC_PRE_PUBLISHING("HP"),
	TS_PRE_PUBLISHING("TSP");



	private String code;

	private ActivityEnum(String inCode) {
		code = inCode;
	}

	public String getCode() {
		return code;
	}

}