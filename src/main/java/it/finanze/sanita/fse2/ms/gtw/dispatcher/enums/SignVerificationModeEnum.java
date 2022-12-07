/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;
import lombok.Getter;

public enum SignVerificationModeEnum {

	NONE("N"),
	TODAY("T"),
	SIGNING_DAY("S");

	@Getter
	private String code;

	private SignVerificationModeEnum(String inCode) {
		code = inCode;
	}

	public static SignVerificationModeEnum get(String signVerificationMode) {
		SignVerificationModeEnum out = null;
		for (SignVerificationModeEnum v:SignVerificationModeEnum.values()) {
			if (v.getCode().equalsIgnoreCase(signVerificationMode)) {
				out = v;
				break;
			}
		}
		return out;
	}

}