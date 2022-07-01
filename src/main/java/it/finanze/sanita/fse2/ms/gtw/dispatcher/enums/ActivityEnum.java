package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum ActivityEnum {

	VERIFICA("V"), 
	VALIDATION("P");



	private String code;

	private ActivityEnum(String inCode) {
		code = inCode;
	}

	public String getCode() {
		return code;
	}

}