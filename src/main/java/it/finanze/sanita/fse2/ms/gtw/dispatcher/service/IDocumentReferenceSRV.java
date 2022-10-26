/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;

public interface IDocumentReferenceSRV extends Serializable{

	ResourceDTO createFhirResources(String cda, PublicationCreationReqDTO requestBody, Integer size, String hash,
			String sourcePatientId, String transformId, String structureId);

}
