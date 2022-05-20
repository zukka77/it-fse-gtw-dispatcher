package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum LowLevelDocEnum {

	
	PRESCRIZIONE("2.16.840.1.113883.2.9.10.1.2","Prescrizione"),
	REFERTO_LABORATORIO("2.16.840.1.113883.2.9.10.1.1","Referto di Laboratorio"),
	PROFILO_SANITARIO_SINTETICO("2.16.840.1.113883.2.9.10.2.4.1.1","Profilo Sanitario Sintetico"),
	PDF("PDF","PDF"),
	TXT("TXT","TXT"),
	LETTERA_DIMISSIONE_OSPEDALIERA("2.16.840.1.113883.2.9.10.1.5","Lettera di Dimissione Ospedaliera"),
	REFERTO_RADIOLOGIA("2.16.840.1.113883.2.9.10.1.7","Referto di Radiologia"),
	EROGATO_SISTEMATS("SistemaTSPrestazione","Erogato SistemaTS"),
	PRESCRIZIONE_SISTEMATS("SistemaTSEsenzione","Prescrizione SistemaTS"),
	ESENZIONE_REDDITO_SISTEMATS("SistemaTSPrescrizione","Esenzione da reddito SistemaTS");

	private String code;
	private String description;

	private LowLevelDocEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

}