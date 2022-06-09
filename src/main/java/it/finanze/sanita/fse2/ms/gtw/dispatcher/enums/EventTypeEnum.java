package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum EventTypeEnum {

	VALIDATION("Validation"),
	PUBLICATION("Publication"),
	FEEDING("Feeding"),
	HISTORICAL_VALIDATION("Historical validation"),
	HISTORICAL_PUBLICATION("Historical publication"),
	GENERIC_ERROR("Generic error from dispatcher");

	private String name;

	private EventTypeEnum(String inName) {
		name = inName;
	}

	public String getName() {
		return name;
	}

}