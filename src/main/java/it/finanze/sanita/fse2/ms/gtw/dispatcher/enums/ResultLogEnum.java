/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;
import lombok.Getter;

@Getter
public enum ResultLogEnum implements ILogEnum {

	OK("OK", "Operazione eseguita con successo"),
	KO("KO", "Errore nell'esecuzione dell'operazione"); 

	private String code;

	private String description;

	private ResultLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}
