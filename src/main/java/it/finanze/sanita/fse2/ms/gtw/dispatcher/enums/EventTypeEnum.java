/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

public enum EventTypeEnum {

	VALIDATION("VALIDATION"),
	VALIDATION_FOR_PUBLICATION("VALIDATION_FOR_PUBLICATION"),
	VALIDATION_FOR_REPLACE("VALIDATION_FOR_REPLACE"),
	PUBLICATION("PUBLICATION"),
	REPLACE("REPLACE"),
	FEEDING("FEEDING"),
	DELETE("DELETE"),
	RIFERIMENTI_INI("RIFERIMENTI_INI"),
	EDS_DELETE("EDS_DELETE"),
	EDS_UPDATE("EDS_UPDATE"),
	INI_DELETE("INI_DELETE"),
	INI_UPDATE("INI_UPDATE"),
	UPDATE("UPDATE"),
	GENERIC_ERROR("Generic error from dispatcher");

	@Getter
	private String name;

	private EventTypeEnum(String inName) {
		name = inName;
	}

}
