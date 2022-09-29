package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

public enum RegionCodeEnum {

	SASN("001","SASN"),
	PIEMONTE("010","Regione Piemonte"),
	VALLE_AOSTA("020","Regione Valle d’Aosta"),
	LOMBARDIA("030","Regione Lombardia"),
	PA_BOLZANO("041","P.A. Bolzano"),
	PA_TRENTO("042","P.A. Trento"),
	VENETO_("050","Regione Veneto"),
	FRIULI_VENEZIA_GIULIA("060","Regione Friuli Venezia Giulia"),
	LIGURIA("070","Regione Liguria"),
	EMILIA_ROMAGNA("080","Regione Emilia-Romagna"),
	TOSCANA("090","Regione Toscana"),
	UMBRIA("100","Regione Umbria"),
	MARCHE("110","Regione Marche"),
	LAZIO("120","Regione Lazio"),
	ABRUZZO("130","Regione Abruzzo"),
	MOLISE("140","Regione Molise"),
	CAMPANIA("150","Regione Campania"),
	PUGLIA("160","Regione Puglia"),
	BASILICATA("170","Regione Basilicata"),
	CALABRIA("180","Regione Calabria"),
	SICILIA("190","Regione Sicilia"),
	SARDEGNA("200","Regione Sardegna"),
	MS("999","Ministero della Salute"),
	INI("000","INI");
	
	@Getter
	private String code;
	
	@Getter
	private String description;

	private RegionCodeEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}
