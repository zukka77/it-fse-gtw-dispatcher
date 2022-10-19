/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

public enum EventTypeEnum {

	VALIDATION("VALIDATION"),
	PUBLICATION("PUBLICATION"),
	REPLACE("REPLACE"),
	FEEDING("FEEDING"),
	GENERIC_ERROR("Generic error from dispatcher");

	@Getter
	private String name;

	private EventTypeEnum(String inName) {
		name = inName;
	}

}
