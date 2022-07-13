package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import lombok.Data;

@Data
public class ValidationDataDTO {
    
    private String hash;

    private boolean cdaValidated;

    private String workflowInstanceId;
}
