/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;
import lombok.Getter;

public enum InjectionModeEnum {

	ATTACHMENT("A"),
	RESOURCE("R");

	@Getter
	private String code;

	private InjectionModeEnum(String inCode) {
		code = inCode;
	}
}