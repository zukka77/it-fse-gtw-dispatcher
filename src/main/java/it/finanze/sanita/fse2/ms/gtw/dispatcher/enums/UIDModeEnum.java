package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

public enum UIDModeEnum {
    
    IP_UUID(1, "IP_UUID"),
    HOSTNAME_UUID(2, "HOSTNAME_UUID"),
    UUID_UUID(3, "UUID_UUID");

    @Getter
    private Integer id;

    @Getter
    private String description;

    private UIDModeEnum(Integer inId, String inDescription) {
        id = inId;
        description = inDescription;
    }

    public static UIDModeEnum get(Integer inId) {
        UIDModeEnum out = null;
        for (UIDModeEnum v : UIDModeEnum.values()) {
            if (v.getId().equals(inId)) {
                out = v;
                break;
            }
        }
        return out;
    }
}
