/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum TipoDocAltoLivEnum {

	WOR("WOR", "Documento di workflow"),
	REF("REF","Referto"),
	LDO("LDO","Lettera di dimissione ospedaliera"),
	RIC("RIC","Richiesta"),
	SUM("SUM","Sommario"),
	TAC("TAC","Taccuino"),
	PRS("PRS","Prescrizione"),
	PRE("PRE","Prestazioni"),
	ESE("ESE","Esenzione"),
	PDC("PDC","Piano di cura"),
	VAC("VAC","Vaccino"),
	CER("CER","Certificato per DGC"),
	VRB("VRB","Verbale");

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