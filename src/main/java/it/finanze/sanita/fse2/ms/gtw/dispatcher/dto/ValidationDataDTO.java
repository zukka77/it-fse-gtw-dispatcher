package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationDataDTO {
    
    private String hash;

    private boolean cdaValidated;

    private String workflowInstanceId; 
    
    private String transformId; 
    
    private String structureId; 
}
