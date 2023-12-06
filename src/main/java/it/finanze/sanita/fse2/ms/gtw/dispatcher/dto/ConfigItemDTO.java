package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ConfigItemDTO {
    private String key;
    private Map<String, String> items;
}
