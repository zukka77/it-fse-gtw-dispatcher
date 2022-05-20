package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum TipoDocAltoLivEnum {

	DOCUMENTO_WORKFLOW("WOR", "Documento di workflow"),
	REFERTO("REF","Referto"),
	LETTERA_DIMISSIONE_OSPEDALIERA("LDO","Lettera di dimissione ospedaliera"),
	RICHIESTA("RIC","Richiesta"),
	SOMMARIO("SUM","Sommario"),
	TACCUINO("TAC","Taccuino"),
	PRESCRIZIONE("PRS","Prescrizione"),
	PRESTAZIONI("PRE","Prestazioni"),
	ESENZIONE("ESE","Esenzione");

	private String code;
	private String description;

	private TipoDocAltoLivEnum(String inCode, String inDescription) {
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