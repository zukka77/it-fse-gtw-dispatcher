/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IStatusCheckClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TransactionInspectResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.NoRecordFoundException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StatusCheckClient implements IStatusCheckClient {

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private MicroservicesURLCFG urlCFG;
	

	@Override
	public TransactionInspectResDTO callSearchEventByWorkflowInstanceId(final String workflowInstanceId) {
		String url =  urlCFG.getStatusCheckClientHost() + "/v1/" + workflowInstanceId;

		TransactionInspectResDTO out = null;
		try {
			out = restTemplate.getForEntity(url, TransactionInspectResDTO.class).getBody();
		} catch (HttpStatusCodeException e1) {
			errorHandler(e1);
		} catch (Exception e) {
			log.error("Errore durante l'invocazione dell' API call search event by wii.", e);
			throw new BusinessException("Errore durante l'invocazione dell' API call search event by wii.", e);
		}

		return out;
	}

	@Override
	public TransactionInspectResDTO callSearchEventByTraceId(final String traceId) {
		String url = urlCFG.getStatusCheckClientHost() + "/v1/search/" + traceId;
		TransactionInspectResDTO out = null;
		try {	
			out = restTemplate.getForEntity(url, TransactionInspectResDTO.class).getBody();
		} catch (HttpStatusCodeException e1) {
			errorHandler(e1);
		} catch (Exception e) {
			log.error("Errore durante l'invocazione dell' API call search event by traceid.", e);
			throw new BusinessException("Errore durante l'invocazione dell' API call search event by traceid.", e);
		}
		return out; 
	}

	@Override
	public TransactionInspectResDTO callSearchEventByIdDocumento(final String idDocumento) {
		String url = urlCFG.getStatusCheckClientHost() + "/v1/search/event/" + idDocumento;
		TransactionInspectResDTO out = null;
		try {	
			out = restTemplate.getForEntity(url, TransactionInspectResDTO.class).getBody();
		} catch (HttpStatusCodeException e1) {
			errorHandler(e1);
		} catch (Exception e) {
			log.error("Errore durante l'invocazione dell' API call search event by traceid.", e);
			throw new BusinessException("Errore durante l'invocazione dell' API call search event by traceid.", e);
		}
		return out; 
	}

	
	private void errorHandler(final HttpStatusCodeException e1) {
		String msg = null;
		
		// 404 Not found.
		if (HttpStatus.NOT_FOUND.equals(e1.getStatusCode())) {
			ErrorResponseDTO error = ErrorResponseDTO.builder().detail("No Record Found").build();
			throw new NoRecordFoundException(error,e1.getStatusCode().value());
		}
		
		// 500 Internal Server Error.
		if (HttpStatus.INTERNAL_SERVER_ERROR.equals(e1.getStatusCode())) {
			throw new BusinessException(msg, e1);
		}
	}
}
