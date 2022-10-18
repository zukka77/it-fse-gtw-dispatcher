package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentTypeEnum {
    
    LAB("2.16.840.1.113883.2.9.10.1.1", "Referto di Laboratorio"),
    RAD("2.16.840.1.113883.2.9.10.1.7.1", "Referto Radiologico"),
    LDO("2.16.840.1.113883.2.9.10.1.5", "Lettera di dimissione ospedaliera"),
    VPS("2.16.840.1.113883.2.9.10.1.6.1", "Verbale di Pronto Soccorso"),
    RSA("2.16.840.1.113883.2.9.10.1.9.1", "Nota di consulto"),
    PSS("2.16.840.1.113883.2.9.10.1.4.1.1", "Profilo Sanitario Sintetico"),
    SINGLE_VAC("2.16.840.1.113883.2.9.10.1.11.1.1", "Immunization note"),
    VAC("2.16.840.1.113883.2.9.10.1.11.1.2", "Immunization summary report");

    private String templateId;

    private String documentType;

    public static DocumentTypeEnum getByTemplateId(String templateId) {
        for (DocumentTypeEnum documentTypeEnum : DocumentTypeEnum.values()) {
            if (documentTypeEnum.getTemplateId().equals(templateId)) {
                return documentTypeEnum;
            }
        }
        return null;
    }

}
