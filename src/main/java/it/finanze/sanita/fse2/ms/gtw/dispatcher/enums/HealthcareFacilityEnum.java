package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum HealthcareFacilityEnum {

	OSPEDALE("Ospedale"),
	PREVENZIONE("Prevenzione"),
	TERRITORIO("Territorio"),
	SISTEMATS("SistemaTS");

	private String code;

	private HealthcareFacilityEnum(String inCode) {
		code = inCode;
	}

	public String getCode() {
		return code;
	}

}