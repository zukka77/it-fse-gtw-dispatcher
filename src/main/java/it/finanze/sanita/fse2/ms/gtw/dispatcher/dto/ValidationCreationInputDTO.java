package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import org.jsoup.nodes.Document;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import lombok.Data;

@Data
public class ValidationCreationInputDTO {
    
    private ValidationDataDTO validationData;

    private JWTTokenDTO jwtToken;

    private String cda;

    private PublicationCreationReqDTO jsonObj;
    
    private byte[] file;
    
    private Document document;

    private String kafkaKey;

    private String documentSha;

    private ResourceDTO fhirResource;

    private ValidationException validationError;
}
