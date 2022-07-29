package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum EventStatusEnum {

	SUCCESS("SUCCESS"),
	BLOCKING_ERROR("BLOCKING_ERROR"),
	NON_BLOCKING_ERROR("NON_BLOCKING_ERROR");

	private final String name;

	EventStatusEnum(String inName) {
		name = inName;
	}

	public String getName() {
		return name;
	}

}