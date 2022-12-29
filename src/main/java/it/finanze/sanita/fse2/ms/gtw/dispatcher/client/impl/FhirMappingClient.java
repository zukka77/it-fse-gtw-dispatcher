/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
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
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.TransformResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FhirMappingClient implements IFhirMappingClient {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private MicroservicesURLCFG msUrlCFG;

	@Override
	public TransformResDTO callConvertCdaInBundle(final FhirResourceDTO resourceToConvert, final String url) {
		TransformResDTO out = null;
		log.debug("Fhir Mapping Client - Calling Fhir Mapping to execute conversion");
		ResponseEntity<TransformResDTO> response = null;
		String fullurl = msUrlCFG.getFhirMappingEngineHost() + url;
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		HttpEntity<?> entity = new HttpEntity<>(resourceToConvert, headers);
		try {
			response = restTemplate.exchange(fullurl, HttpMethod.POST, entity, TransformResDTO.class);
			out = response.getBody();
		} catch(ResourceAccessException cex) {
			log.error("Connect error while call document transform :" + msUrlCFG.getFhirMappingEngineHost(),cex);
			throw new ConnectionRefusedException(msUrlCFG.getFhirMappingEngineHost(),"Connection refused");
		}  
		return out;
	}
}
