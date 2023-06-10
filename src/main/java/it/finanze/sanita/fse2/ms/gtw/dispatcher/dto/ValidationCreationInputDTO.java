/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import lombok.Data;
import org.jsoup.nodes.Document;

@Data
public class ValidationCreationInputDTO {
    
    private ValidationDataDTO validationData;

    private JWTPayloadDTO jwtPayloadToken;

    private String cda;

    private PublicationCreationReqDTO jsonObj;
    
    private byte[] file;
    
    private Document document;

    private String kafkaKey;

    private String documentSha;

    private ResourceDTO fhirResource;
    
}
