/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;

public interface IIniEdsInvocationSRV extends Serializable {
	
	Boolean insert(String workflowInstanceId, ResourceDTO fhirResourceDTO, JWTTokenDTO jwtToken);

	Boolean replace(String workflowInstanceId, ResourceDTO fhirResourceDTO, JWTTokenDTO jwtToken, final String identificativoDocumento);

}
