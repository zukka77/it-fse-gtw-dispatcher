package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IFhirMappingClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.DocumentReferenceResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FhirMappingClient implements IFhirMappingClient {

	/**
	 * Serial version uid. 
	 */
	private static final long serialVersionUID = 6009573115047546392L;

	@Autowired
    private transient RestTemplate restTemplate;
	
	@Autowired
	private MicroservicesURLCFG msUrlCFG;

	@Override
	public DocumentReferenceResDTO callConvertCdaInBundle(final FhirResourceDTO resourceToConvert) {
		DocumentReferenceResDTO out = null;
		log.debug("Fhir Mapping Client - Calling Fhir Mapping to execute conversion");
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		HttpEntity<?> entity = new HttpEntity<>(resourceToConvert, headers);

		ResponseEntity<DocumentReferenceResDTO> response = null;
		try {
			response = restTemplate.exchange(msUrlCFG.getFhirMappingHost() + "/v1/document-reference", HttpMethod.POST, entity, DocumentReferenceResDTO.class);
			out = response.getBody();
			log.debug("{} status returned from Fhir Mapping Client", response.getStatusCode());
		} catch(ResourceAccessException cex) {
			log.error("Connect error while call document reference ep :" + cex);
			throw new ConnectionRefusedException(msUrlCFG.getFhirMappingHost(),"Connection refused");
		} catch(Exception ex) {
			log.error("Generic error while call document reference ep :" + ex);
			throw new BusinessException("Generic error while call document reference ep :" + ex);
		}
		return out;
	}
}
