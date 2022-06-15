package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;

public interface IIniEdsInvocationSRV extends Serializable {
	
	Boolean insert(String transactionId, FhirResourceDTO fhirResourceDTO, JWTTokenDTO jwtToken);
}
