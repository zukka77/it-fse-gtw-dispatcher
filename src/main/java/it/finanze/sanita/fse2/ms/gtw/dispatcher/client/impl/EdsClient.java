/**
 * 
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.exceptions.ServerResponseException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author AndreaPerquoti
 *
 */
@Slf4j
@Component
public class EdsClient implements IEdsClient {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1497611210936387510L;
	
	@Autowired
	private RestTemplate rt;
	
	@Autowired
	private MicroservicesURLCFG msUrlCFG;
	
	@Override
	public Object delete(final String oid) {
		Object output = null;
		
		try {
			log.info("client.delete()");

			// Build headers.
			HttpEntity<Object> entity = new HttpEntity<>(oid, null);
			
			// Build endpoint e call.
			String endpoint = buildEndpoint("/v1/eds-delete");
			ResponseEntity<Object> restExchange = rt.exchange(endpoint, HttpMethod.POST, entity, new ParameterizedTypeReference<Object>() {});
			
			// Gestione response
			if (HttpStatus.OK.equals(restExchange.getStatusCode()) && restExchange.getBody() != null) {
				output = restExchange.getBody();
			}
			log.info("client.delete()");
		} catch (HttpStatusCodeException e1) {
			errorHandler(e1, "/delete");
		} catch (Exception e) {
			log.error("Errore durante l'invocazione dell' API delete(). ", e);
			throw new BusinessException("Errore durante l'invocazione dell' API delete(). ", e);
		}
		
		return output;
	}

	/**
	 * Error handler.
	 *
	 * @param e1 the e 1
	 * @param endpoint the endpoint
	 */
	private void errorHandler(HttpStatusCodeException e1, String endpoint) {
		// Generic handler
		String msg = "Errore durante l'invocazione dell' API " + endpoint + ". Il sistema ha restituito un " + e1.getStatusCode();
		throw new ServerResponseException(endpoint, msg, e1.getStatusCode(), e1.getRawStatusCode(), e1.getLocalizedMessage());
	}
	
	/**
	 * Builder endpoint Settings API.
	 *
	 * @param endpoint the endpoint
	 * @return the string
	 */
	private String buildEndpoint(final String endpoint) {
		// Build dell'endpoint da invocare.
		StringBuilder sb = new StringBuilder(msUrlCFG.getEdsClientHost()); // Base URL host
		sb.append(endpoint);
		return sb.toString();
	}
	
	/**
	 * Build header necessario per l'Autenticazione del servizio chiamante.
	 *
	 * @return the http headers
	 */
	private HttpHeaders buildHeaders() {
		// PROVVISORI
		List<String> values = new ArrayList<>();
		values.add(MediaType.APPLICATION_JSON_VALUE);
		values.add(StandardCharsets.UTF_8.name());

		HttpHeaders headers = new HttpHeaders();
		headers.put(HttpHeaders.ACCEPT, values);
		headers.put(HttpHeaders.CONTENT_TYPE, values);
		
		return headers;
	}
}
