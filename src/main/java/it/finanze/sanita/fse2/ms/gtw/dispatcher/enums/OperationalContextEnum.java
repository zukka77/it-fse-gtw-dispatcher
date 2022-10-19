/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum OperationalContextEnum {

	TREATMENT("TREATMENT","Trattamento di cura ordinario"),
	EMERGENCY("EMERGENCY","Trattamento in emergenza"),
	PUBEMERGENCY("PUBEMERGENCY","Trattamento per la salvaguardia di un terzo o della collettività"),
	SYSADMIN("SYSADMIN","Trasferimento del FSE"),
	PERSONAL("PERSONAL","Consultazione del FSE"),
	UPDATE("UPDATE","Invalidamento e aggiornamento di un documento"),
	CONSENT("CONSENT","Comunicazione valori consensi"),
	ADMINISTRATIVE("ADMINISTRATIVE","Operazioni amministrative");

	private String code;
	private String description;

	private OperationalContextEnum(String inCode, String inDescription) {
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