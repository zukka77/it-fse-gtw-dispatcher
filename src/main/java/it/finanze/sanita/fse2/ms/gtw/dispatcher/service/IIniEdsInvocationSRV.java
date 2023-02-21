/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ResourceDTO;

public interface IIniEdsInvocationSRV {
	
	Boolean insert(String workflowInstanceId, ResourceDTO fhirResourceDTO, JWTPayloadDTO jwtPayloadToken);

	Boolean replace(String workflowInstanceId, ResourceDTO fhirResourceDTO, JWTPayloadDTO jwtPayloadToken, String identificativoDocumento);

}
