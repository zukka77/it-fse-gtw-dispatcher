package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum RoleEnum {

	PERSONALE_ASSISTENZA_ALTA_SPECIALIZZAZIONE("AAS","Personale di assistenza ad alta specializzazione"),
	MEDICO_MEDICINA_GENERALE_PEDIATRA_LIBERA_SCELTA("APR","Medico Medicina Generale Pediatra di Libera Scelta"),
	PROFESSIONISTA_SOCIALE("PSS","Professionista del sociale"),
	PERSONALE_INFERMIERISTICO("INF","Personale infermieristico"),
	FARMACISTA("FAR","Farmacista"),
	DIRETTORE_SANITARIO("DSA","Direttore sanitario"),
	DIRETTORE_AMMINISTRATIVO("DAM","Direttore amministrativo"),
	OPERATORE_AMMINISTRATIVO("OAM","Operatore amministrativo"),
	ASSISTITO("ASS","Assistito"),
	TUTORE("TUT","Tutore"),
	INFORMAL_GIVER("ING","Informal giver (Assistito)"),
	GENITORE_ASSISTITO("GEN","Genitore Assistito"),
	NODO_REGIONALE("NOR","Nodo regionale"),
	DIRIGENTE_SANITARIO("DRS","Dirigente sanitario"),
	MEDICO_RSA("RSA","Medico RSA"),
	MEDICO_RETE_PATOLOGIA("MRP","Medico Rete di Patologia"),
	INFRASTRUTTURA_NAZIONALE_INTEROPERABILITÀ("INI","Infrastruttura Nazionale per l’Interoperabilità"),
	OPERATORE_GESTIONE_CONSENSI("OGC","Operatore per la gestione dei consensi"),
	OPERATORE_INFORMATIVA("OPI","Operatore di informativa");

	private String code;
	private String description;

	private RoleEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

}