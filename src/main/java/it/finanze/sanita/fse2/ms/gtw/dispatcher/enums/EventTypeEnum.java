package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum EventTypeEnum {

	VALIDATION("VALIDATION"),
	PUBLICATION("PUBLICATION"),
	FEEDING("FEEDING"),
	GENERIC_ERROR("Generic error from dispatcher");

	private String name;

	private EventTypeEnum(String inName) {
		name = inName;
	}

	public String getName() {
		return name;
	}

}