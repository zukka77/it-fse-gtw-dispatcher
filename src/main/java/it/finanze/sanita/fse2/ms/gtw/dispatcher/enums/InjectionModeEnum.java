package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum InjectionModeEnum {

	ATTACHMENT("A"),
	RESOURCE("R");

	private String code;

	private InjectionModeEnum(String inCode) {
		code = inCode;
	}


	public String getCode() {
		return code;
	}
}