/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum IniMimeTypeEnum {

	CDA("text/x-cda-r2+xml"),
	XML("text/xml"),
	PLAIN("text/plain"),
	PKCS7("application/x-pkcs7-mime"),
	RTF("application/rtf"),
	PDF("application/pdf"),
	MULTIPART("multipart/related"),
	DICOM("application/dicom"),
	JSON("application/json");

	private String code;

	private IniMimeTypeEnum(String inCode) {
		code = inCode;
	}

	public String getCode() {
		return code;
	}

}