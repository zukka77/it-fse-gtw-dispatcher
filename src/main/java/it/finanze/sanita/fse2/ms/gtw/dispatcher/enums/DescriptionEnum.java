package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

@Getter
public enum DescriptionEnum {

	DESC_OID1("2.16.840.1.113883.2.9.6.1.5"),
	DESC_OID2("2.16.840.1.113883.2.9.6.1.51"),
	DESC_OID3("2.16.840.1.113883.6.73"),
	DESC_OID4("2.16.840.1.113883.2.9.6.1.11"),
	DESC_OID5("2.16.840.1.113883.2.9.2.COD_REGIONE.6.1.11"),
	DESC_OID6("2.16.840.1.113883.2.9.6.1.5"),
	DESC_OID7("2.16.840.1.113883.6.73");

	private String oid;
	
	private DescriptionEnum(String inOid) {
		oid = inOid;
	}
}