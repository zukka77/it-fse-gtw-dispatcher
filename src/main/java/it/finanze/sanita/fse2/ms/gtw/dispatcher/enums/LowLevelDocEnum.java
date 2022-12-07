/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;
import lombok.Getter;

@Getter
public enum LowLevelDocEnum {
	
	PRESCRIZIONE("2.16.840.1.113883.2.9.10.1.2","Prescrizione"),
	REFERTO_LABORATORIO("2.16.840.1.113883.2.9.10.1.1","Referto di Laboratorio"),
	PROFILO_SANITARIO_SINTETICO("2.16.840.1.113883.2.9.10.2.4.1.1","Profilo Sanitario Sintetico"),
	PDF("PDF","PDF"),
	TXT("TXT","TXT"),
	LETTERA_DIMISSIONE_OSPEDALIERA("2.16.840.1.113883.2.9.10.1.5","Lettera di Dimissione Ospedaliera"),
	REFERTO_RADIOLOGIA("2.16.840.1.113883.2.9.10.1.7","Referto di Radiologia"),
	PIANO_TERAPEUTICO("2.16.840.1.113883.2.9.4.3.14","Piano Terapeutico"),
	SCHEDA_SINGOLA_VACCINAZIONE("2.16.840.1.113883.2.9.10.1.11.1.1","Scheda della singola Vaccinazione"),
	CERTIFICATO_VACCINALE("2.16.840.1.113883.2.9.10.1.11.1.2","Certificato Vaccinale"),
	VERBALE_PRONTO_SOCCORSO("2.16.840.1.113883.2.9.10.1.6.1","Verbale di Pronto Soccorso"),
	REFERTO_SPECIALISTICA_AMBULATORIALE("2.16.840.1.113883.2.9.10.1.9.1","Referto di Specialistica Ambulatoriale"),
	EROGATO_SISTEMATS("SistemaTS-Prestazione","Erogato SistemaTS"),
	PRESCRIZIONE_SISTEMATS("SistemaTS-Esenzione","Prescrizione SistemaTS"),
	ESENZIONE_REDDITO_SISTEMATS("SistemaTS-Prescrizione","Esenzione da reddito SistemaTS");

	private String code;
	private String description;

	private LowLevelDocEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}