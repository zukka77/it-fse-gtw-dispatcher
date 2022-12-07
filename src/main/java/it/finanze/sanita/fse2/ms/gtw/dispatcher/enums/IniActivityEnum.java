/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

@Getter
public enum IniActivityEnum {

	CREATE("CREATE", "Creazione"),
	READ("READ", "Lettura"),
	UPDATE("UPDATE", "Aggiornamento"),
	DELETE("DELETE", "Cancellazione");

	private String code;
	private String description;

	private IniActivityEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}