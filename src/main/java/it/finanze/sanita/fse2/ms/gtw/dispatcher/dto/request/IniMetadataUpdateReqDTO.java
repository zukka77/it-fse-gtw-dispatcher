/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IniMetadataUpdateReqDTO {
	
    private String marshallData;
    
    private JWTPayloadDTO token;
    
    private String documentType;
    
    private String workflow_instance_id;
    
}
