package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum AttivitaClinicaEnum {

	PERSONAL_HEALTH_RECORD_UPDATE("PHR", "Personal Health Record Update"),
	CONSULTO("CON", "Consulto"),
	DISCHARGE("DIS", "Discharge"),
	EROGAZIONE_PRESTAZIONE_PRENOTATA("ERP", "Erogazione Prestazione Prenotata"),
	DOCUMENTI_SISTEMA_TS("Sistema TS", "Documenti sistema TS");

	private String code;
	private String description;

	private AttivitaClinicaEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public String getDescription() {
		return description;
	}

	public String getCode() {
		return code;
	}

}