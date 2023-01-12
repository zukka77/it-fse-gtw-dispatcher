package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum ActionEnum {

	CREATE, READ, UPDATE, DELETE;

	public static ActionEnum get(String action_id) {
		for (ActionEnum v : ActionEnum.values()) {
			if (v.name().equals(action_id)) {
				return v;
			}
		}
		return null;
	}
}