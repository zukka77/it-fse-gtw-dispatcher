package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public enum HealthDataFormatEnum {

	CDA("V"),  
	DICOM_SR("D");

	private String code;

	private HealthDataFormatEnum(String inCode) {
		code = inCode;
	}
	
	public String getCode() {
		return code;
	}

}