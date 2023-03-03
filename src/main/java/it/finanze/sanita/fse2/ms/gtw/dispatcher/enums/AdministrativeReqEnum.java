package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

@Getter
public enum AdministrativeReqEnum {

	SSN("SSN", "Regime SSN"),
	INPATIENT("INPATIENT", "Regime di ricovero"),
	NOSSN("NOSSN", "Regime privato"),
	SSR("SSR", "Regime SSR"),
	DONOR("DONOR", "Regime donatori ");

	private String code;
	private String description;

	private AdministrativeReqEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}