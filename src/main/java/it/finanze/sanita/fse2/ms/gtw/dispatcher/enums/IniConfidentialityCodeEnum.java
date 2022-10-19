/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum IniConfidentialityCodeEnum {

	UNRESTRICTED("U","Unrestricted"),
	LOW("L","Low"),
	MODERATE("M","Moderate"),
	NORMAL("N","Normal"),
	RESTRICTED("R","Restricted"),
	VERY_RESTRICTED("V","Very Restricted");

	private String code;
	private String description;

	private IniConfidentialityCodeEnum(String inCode, String inDescription) {
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