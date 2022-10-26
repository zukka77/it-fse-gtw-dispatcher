/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import java.util.Date;

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
    
    private Date insertionDate; 
    
}
