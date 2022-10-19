/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

public enum EventCodeEnum {

	P99("P99", "Oscuramento del documento"),
	J07BX03("J07BX03", "Vaccino per Covid-19"),
	LP418019_8("LP418019-8", "Tampone antigenico per Covid-19"),
	LP417541_2("LP417541-2", "Tampone molecolare per Covid-19"),
	_96118_5("96118-5", "Test Sierologico qualitativo"),
	_94503_0("94503-0", "Test Sierologico quantitativo"),
	pay("pay", "Prescrizione farmaceutica non a carico SSN"),
	PUBLICPOL("PUBLICPOL", "Prescrizione farmaceutica SSN");

	@Getter
	private String code;

	@Getter
	private String description;

	private EventCodeEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public static EventCodeEnum fromValue(final String code) {
		EventCodeEnum output = null;
        for (EventCodeEnum valueEnum : EventCodeEnum.values()) {
        	if (valueEnum.getCode().equalsIgnoreCase(code)) {
        		output = valueEnum;
        		break;
        	}

        }
		 
		return output;
    }
}
