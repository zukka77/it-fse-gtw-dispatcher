/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.base.AbstractClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EdsClient extends AbstractClient implements IEdsClient {

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private MicroservicesURLCFG msUrlCFG;
	
	@Override
	public EdsResponseDTO delete(final String oid) {
		EdsResponseDTO output = null;
		
		log.debug("EDS Client - Calling EDS to execute delete operation");
		String endpoint = msUrlCFG.getEdsClientHost() + Constants.Client.Eds.DELETE_PATH.replace(Constants.Client.Eds.ID_DOC_PLACEHOLDER, oid);
		try {
			ResponseEntity<EdsResponseDTO> restExchange = restTemplate.exchange(endpoint, HttpMethod.DELETE, null, EdsResponseDTO.class);
			output = restExchange.getBody();
			log.debug("EDS Client - Deletion operation executed successfully");
		} catch (HttpStatusCodeException e1) {
			errorHandler("eds", e1, "/delete");
		} catch (Exception e) {
			log.error("Errore durante l'invocazione di EDS dell' API delete(). ", e);
			throw new BusinessException("Errore durante l'invocazione di EDS dell' API delete(). ", e);
		}
		
		return output;
	}

	@Override
	public EdsResponseDTO update(EdsMetadataUpdateReqDTO req) {
		EdsResponseDTO output = null;

		log.debug("EDS Client - Calling EDS to execute update operation");
		HttpEntity<Object> entity = new HttpEntity<>(req, null);
		// Build endpoint e call.
		String endpoint = msUrlCFG.getEdsClientHost() + Constants.Client.Eds.UPDATE_PATH.replace(Constants.Client.Eds.ID_DOC_PLACEHOLDER, req.getIdDoc());
		try {
			ResponseEntity<EdsResponseDTO> restExchange = restTemplate.exchange(endpoint, HttpMethod.PUT, entity, EdsResponseDTO.class);
			output = restExchange.getBody();
			log.debug("EDS Client - Update operation executed successfully");
		} catch (HttpStatusCodeException e1) {
			errorHandler("eds", e1, "/update");
		} catch (Exception e) {
			log.error("Errore durante l'invocazione di EDS dell' API update(). ", e);
			throw new BusinessException("Errore durante l'invocazione di EDS dell' API update(). ", e);
		}
		return output;
	}
	
}
