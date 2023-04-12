package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

public enum SystemTypeEnum {

    TS("TS"),
    NONE(null);

    private final String name;

    SystemTypeEnum(String name) {
        this.name = name;
    }

    public String value() {
        return name;
    }
}
