/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IValidatorClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.base.AbstractClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.client.ValidationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.ValidationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import lombok.extern.slf4j.Slf4j;

/**
 * Production implemention of Validator Client.
 */
@Slf4j
@Component
public class ValidatorClient extends AbstractClient implements IValidatorClient {


	@Autowired
    private RestTemplate restTemplate;
	
	@Autowired
	private MicroservicesURLCFG msUrlCFG;

    @Override
    @CircuitBreaker(name = "validationCDA")
    public ValidationInfoDTO validate(final String cda) {
        log.debug("Validator Client - Calling Validator to execute validation of CDA");
        ValidationInfoDTO out = null;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        
        ValidationReqDTO req = new ValidationReqDTO();
        req.setCda(cda);

        HttpEntity<?> entity = new HttpEntity<>(req, headers);
        
        ValidationResDTO response = null;
        try {
        	response = restTemplate.postForObject(msUrlCFG.getValidatorHost() + "/v1/validate", entity, ValidationResDTO.class);
        	if(response!=null) {
        		out = response.getResult();
        	}
        } catch(ResourceAccessException cex) {
        	log.error("Connect error while call validation ep :" + cex);
        	throw new ConnectionRefusedException(msUrlCFG.getValidatorHost(),"Connection refused");
		}  
        
        return out;
    }

}
