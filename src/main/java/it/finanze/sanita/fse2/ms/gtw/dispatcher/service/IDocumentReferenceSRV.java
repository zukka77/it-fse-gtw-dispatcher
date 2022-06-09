package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;

public interface IDocumentReferenceSRV extends Serializable{

	FhirResourceDTO createFhirResources(String cda, PublicationCreationReqDTO requestBody, Integer size, String hash);
}
