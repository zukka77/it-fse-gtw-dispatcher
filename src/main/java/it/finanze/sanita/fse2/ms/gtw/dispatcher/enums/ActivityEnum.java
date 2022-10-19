/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

public enum ActivityEnum {

	VERIFICA("V"), VALIDATION("P");

	@Getter
	private String code;

	private ActivityEnum(String inCode) {
		code = inCode;
	}

}
