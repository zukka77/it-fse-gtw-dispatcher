/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IIniClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.base.AbstractClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.IniClientRoutes;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.DeleteRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.IniMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.IniReferenceRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.MergedMetadatiRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.GetMergedMetadatiDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniReferenceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniTraceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.GetMergedMetadatiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.*;

@Slf4j
@Component
public class IniClient extends AbstractClient implements IIniClient {

	@Autowired
	private MicroservicesURLCFG msUrlCFG;

	@Autowired
	private RestTemplate client;

	@Autowired
	private IniClientRoutes routes;

	@Override
	public IniTraceResponseDTO delete(final DeleteRequestDTO request) {

		String endpoint = routes.delete();
		IniTraceResponseDTO output = null;

		log.debug("{} - Executing request: {}", routes.identifier(), endpoint);

		try {
			// Execute request
			ResponseEntity<IniTraceResponseDTO> response = client.exchange(
				endpoint,
				DELETE,
				new HttpEntity<>(request),
				IniTraceResponseDTO.class
			);
			// Retrieve body
			output = response.getBody();
		} catch (RestClientResponseException ex) {
			toServerResponseEx(routes.identifier(), ex, endpoint);
		}

		return output;
	}

	@Override
	public IniReferenceResponseDTO reference(IniReferenceRequestDTO request) {

		String endpoint = routes.references(request.getIdDoc());
		IniReferenceResponseDTO output = null;

		log.debug("{} - Executing request: {}", routes.identifier(), endpoint);

		try {
			// Execute request
			ResponseEntity<IniReferenceResponseDTO> response = client.exchange(
				endpoint,
				POST,
				new HttpEntity<>(request.getToken()),
				IniReferenceResponseDTO.class
			);
			// Retrieve body
			output = response.getBody();
		} catch (RestClientResponseException ex) {
			toServerResponseEx(routes.identifier(), ex, endpoint);
		}

		return output;
	}

	@Override
	public IniTraceResponseDTO update(IniMetadataUpdateReqDTO request) {

		String endpoint = routes.update();
		IniTraceResponseDTO output = null;

		log.debug("{} - Executing request: {}", routes.identifier(), endpoint);

		try {
			// Execute request
			ResponseEntity<IniTraceResponseDTO> response = client.exchange(
				endpoint,
				PUT,
				new HttpEntity<>(request),
				IniTraceResponseDTO.class
			);
			// Retrieve body
			output = response.getBody();
		} catch (RestClientResponseException ex) {
			toServerResponseEx(routes.identifier(), ex, endpoint);
		}

		return output;
	}

	@Override
	public GetMergedMetadatiDTO getMergedMetadati(final MergedMetadatiRequestDTO iniReq) {
		GetMergedMetadatiDTO out = null;
		try {
			log.debug("INI Client - Calling INI to execute update metadati :{}", iniReq.getIdDoc());

			// Build headers.
			HttpEntity<Object> entity = new HttpEntity<>(iniReq, null);

			// Build endpoint e call.
			String endpoint = msUrlCFG.getIniClientHost() + "/v1/getMergedMetadati";
			ResponseEntity<GetMergedMetadatiResponseDTO> restExchange = client.exchange(endpoint, PUT, entity, GetMergedMetadatiResponseDTO.class);
			if(restExchange.getBody()!=null) {
				out = restExchange.getBody().getResponse();
			}
		} catch (Exception e) {
			log.error("Errore durante l'invocazione dell' API update(). ", e);
			throw e;
		}
		return out;
	}
}
