package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum AttivitaClinicaEnum {

	PHR("PHR", "Personal Health Record Update"),
	CON("CON", "Consulto"),
	DIS("DIS", "Discharge"),
	ERP("ERP", "Erogazione Prestazione Prenotata"),
	Sistema_TS("Sistema TS", "Documenti sistema TS");

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