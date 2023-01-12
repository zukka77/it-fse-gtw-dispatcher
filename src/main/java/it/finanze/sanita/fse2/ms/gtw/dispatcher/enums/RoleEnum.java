/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

public enum RoleEnum {

	AAS("AAS", "Personale di assistenza ad alta specializzazione"),
	APR("APR", "Medico Medicina Generale Pediatra di Libera Scelta"),
	PSS("PSS", "Professionista del sociale"),
	INF("INF", "Personale infermieristico"),
	FAR("FAR", "Farmacista"),
	DSA("DSA", "Direttore sanitario"),
	DAM("DAM", "Direttore amministrativo"),
	OAM("OAM", "Operatore amministrativo"),
	ASS("ASS", "Assistito"),
	TUT("TUT", "Tutore"),
	ING("ING", "Informal giver (Assistito)"),
	GEN("GEN", "Genitore Assistito"),
	NOR("NOR", "Nodo regionale"),
	DRS("DRS", "Dirigente sanitario"),
	RSA("RSA", "Medico RSA"),
	MRP("MRP", "Medico Rete di Patologia"),
	INI("INI", "Infrastruttura Nazionale per l’Interoperabilità"),
	OGC("OGC", "Operatore per la gestione dei consensi"),
	OPI("OPI", "Operatore di informativa"),
	MDS("MDS", "Ruolo del Ministero della Salute per la gestione del DGC");

	@Getter
	private String code;
	@Getter
	private final String description;

	RoleEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public static RoleEnum get(String code) {
		for (RoleEnum role : RoleEnum.values()) {
			if (role.getCode().equals(code)) {
				return role;
			}
		}
		return null;
	}

}
