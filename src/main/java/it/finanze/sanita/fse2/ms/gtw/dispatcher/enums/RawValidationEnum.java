/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;
import lombok.Getter;

@Getter
public enum RawValidationEnum {

	OK("00", "OK"),
	SYNTAX_ERROR("01", "Errore di sintassi"),
	VOCABULARY_ERROR("02", "Errore dovuto alle terminologie utilizzate"),
	SEMANTIC_ERROR("03", "Errore semantico"),
	SCHEMATRON_NOT_FOUND("04", "Schematron not found"),
	SEMANTIC_WARNING("05", "Warning semantico");
	
	private String code;
	private String description;

	private RawValidationEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}