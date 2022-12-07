/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public enum HealthDataFormatEnum {

	CDA("V");

	@Getter
	private String code;

	private HealthDataFormatEnum(String inCode) {
		code = inCode;
	}

}