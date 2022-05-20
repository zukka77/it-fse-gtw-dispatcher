package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum SignVerificationModeEnum {

	NONE("N"),
	TODAY("T"),
	SIGNING_DAY("S");

	private String code;

	private SignVerificationModeEnum(String inCode) {
		code = inCode;
	}

	public String getCode() {
		return code;
	}

	public static SignVerificationModeEnum get(String signVerificationMode) {
		SignVerificationModeEnum out = null;
		for (SignVerificationModeEnum v:SignVerificationModeEnum.values()) {
			if (v.getCode().equalsIgnoreCase(signVerificationMode)) {
				out = v;
				break;
			}
		}
		return out;
	}

}