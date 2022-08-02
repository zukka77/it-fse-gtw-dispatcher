/**
 * 
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IIniClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.exceptions.ServerResponseException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.DeleteRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniTraceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author AndreaPerquoti
 *
 */
@Slf4j
@Component
public class IniClient implements IIniClient {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 8054486322408383036L;
	
	@Autowired
	private RestTemplate rt;
	
	@Autowired
	private MicroservicesURLCFG msUrlCFG;
	
	@Override
	public IniTraceResponseDTO delete(final DeleteRequestDTO iniReq) {
		IniTraceResponseDTO output = null;
		
		try {
			log.info("client.delete()");

			// Build headers.
			HttpEntity<Object> entity = new HttpEntity<>(iniReq, null);
			
			// Build endpoint e call.
			String endpoint = buildEndpoint("/v1/ini-delete");
			ResponseEntity<IniTraceResponseDTO> restExchange = rt.exchange(endpoint, HttpMethod.DELETE, entity, IniTraceResponseDTO.class);
			
			// Gestione response
			if (HttpStatus.OK.equals(restExchange.getStatusCode()) && restExchange.getBody() != null) {
				output = restExchange.getBody();
				if(output!=null && Boolean.FALSE.equals(output.getEsito())) {
					throw new ServerResponseException(output.getErrorMessage());
				}
			}  
			log.info("client.delete()");
		} catch(ServerResponseException e0) {
			throw e0;
		} catch (HttpClientErrorException e1) {
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
		StringBuilder sb = new StringBuilder(msUrlCFG.getIniClientHost()); // Base URL host
		sb.append(endpoint);
		return sb.toString();
	}

 
	
	@Override
	public IniTraceResponseDTO updateMetadati(String idDocumento) {
		
		IniTraceResponseDTO out = null;
		try {
			log.info("Update metadati %s : " + idDocumento);

			// Build headers.
			HttpEntity<Object> entity = new HttpEntity<>(idDocumento, null);
			
			// Build endpoint e call.
			String endpoint = buildEndpoint("/v1/ini-delete");
			ResponseEntity<IniTraceResponseDTO> restExchange = rt.exchange(endpoint, HttpMethod.PUT, entity, IniTraceResponseDTO.class);
			
			// Gestione response
			if (HttpStatus.OK.equals(restExchange.getStatusCode()) && restExchange.getBody() != null) {
				out = restExchange.getBody();
				if(out!=null && Boolean.FALSE.equals(out.getEsito())) {
					throw new ServerResponseException(out.getErrorMessage());
				}
			}  
		} catch(ServerResponseException e0) {
			throw e0;
		} catch (HttpStatusCodeException e1) {
			errorHandler(e1, "/ini-update");
		} catch (Exception e) {
			log.error("Errore durante l'invocazione dell' API delete(). ", e);
			throw new BusinessException("Errore durante l'invocazione dell' API delete(). ", e);
		}
		
		return out;
	}
	 
	
}
