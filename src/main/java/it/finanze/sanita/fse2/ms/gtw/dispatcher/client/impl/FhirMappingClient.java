package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IFhirMappingClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.TransformResDTO;
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
	public TransformResDTO callConvertCdaInBundle(final FhirResourceDTO resourceToConvert) {
		TransformResDTO out = null;
		log.debug("Fhir Mapping Client - Calling Fhir Mapping to execute conversion");
		HttpHeaders headers = new HttpHeaders();
		ResponseEntity<TransformResDTO> response = null;
		
		try {
			if(Boolean.TRUE.equals(msUrlCFG.getCallTransformEngine())) {
				headers.setContentType(MediaType.MULTIPART_FORM_DATA);
				// HttpEntity<?> entityMap = new HttpEntity<>(resourceToConvert, headers); // ????
				
				ByteArrayResource fileAsResource = new ByteArrayResource(resourceToConvert.getCda().getBytes()){
					@Override
					public String getFilename(){
						return "file";
					}
				};
				LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
				map.add("file",fileAsResource);

				headers.setContentType(MediaType.MULTIPART_FORM_DATA);

				HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);			
				response = restTemplate.exchange(msUrlCFG.getFhirMappingEngineHost() + "/v1/documents/transform", HttpMethod.POST, requestEntity, TransformResDTO.class);	
				log.info("{} status returned from Fhir mapping engine Client", response.getStatusCode());
			} else {
				headers.set("Content-Type", "application/json");
				HttpEntity<?> entityXslm = new HttpEntity<>(resourceToConvert, headers);
				response = restTemplate.exchange(msUrlCFG.getFhirMappingHost() + "/v1/documents/transform", HttpMethod.POST, entityXslm, TransformResDTO.class);
				log.info("{} status returned from Fhir mapping Client", response.getStatusCode());
			}
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
