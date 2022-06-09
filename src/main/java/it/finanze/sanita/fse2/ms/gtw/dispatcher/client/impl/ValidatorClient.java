package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IValidatorClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.client.ValidationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.ValidationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import lombok.extern.slf4j.Slf4j;

/**
 * Production implemention of Validator Client.
 * 
 * @author CPIERASC
 */
@Slf4j
@Component
@Profile("!" + Constants.Profile.TEST)
public class ValidatorClient extends AbstractClient implements IValidatorClient {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -5382557368894888319L;

	@Autowired
    private transient RestTemplate restTemplate;
	
	@Autowired
	private MicroservicesURLCFG msUrlCFG;

    @Override
    @CircuitBreaker(name = "validationCDA")
    public ValidationInfoDTO validate(final String cda) {
        log.info("Calling Validation Client to validate CDA...");
        ValidationInfoDTO out = null;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        
        ValidationReqDTO req = new ValidationReqDTO();
        req.setCda(cda);

        HttpEntity<?> entity = new HttpEntity<>(req, headers);
        
        ResponseEntity<ValidationResDTO> response = null;
        try {
        	response = restTemplate.exchange(msUrlCFG.getValidatorHost() + "/v1.0.0/validate", HttpMethod.POST, entity, ValidationResDTO.class);
        	if (response != null) {
        		ValidationResDTO result = response.getBody();
        		log.info("{} status returned from Validator Client", response.getStatusCode());
        		log.info("{} body returned from Validator Client", result);
        		if (result!=null) {
        			out = result.getResult();
        		}
        	}
        } catch(RestClientException cex) {
        	log.error("Connect error while call validation ep :" + cex);
        	throw new ConnectionRefusedException(msUrlCFG.getValidatorHost(),"Connection refused");
		} catch(Exception ex) {
        	log.error("Generic error while call validation ep :" + ex);
        	throw new BusinessException("Generic error while call validation ep :" + ex);
        }
        
        return out;
    }

}
