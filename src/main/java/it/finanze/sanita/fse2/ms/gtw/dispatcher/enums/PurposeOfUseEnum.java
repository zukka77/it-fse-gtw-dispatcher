package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum PurposeOfUseEnum {

	TREATMENT("TREATMENT"),
	SYSADMIN("SYSADMIN"),
	UPDATE("UPDATE");

	private String display;

	private PurposeOfUseEnum(String inDisplay) {
		display = inDisplay;
	}

	public String getDisplay() {
		return display;
	}

	public static PurposeOfUseEnum get(String inDisplay) {
		PurposeOfUseEnum out = null;
		for (PurposeOfUseEnum v:PurposeOfUseEnum.values()) {
			if (v.getDisplay().equalsIgnoreCase(inDisplay)) {
				out = v;
				break;
			}
		}
		return out;
	}
}
