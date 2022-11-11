/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IIniClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.base.AbstractClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.IniClientRoutes;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.DeleteRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.IniMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.IniReferenceRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.MergedMetadatiRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.GetMergedMetadatiDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniReferenceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniTraceResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.*;
import static org.springframework.http.HttpMethod.*;

@Slf4j
@Component
public class IniClient extends AbstractClient implements IIniClient {

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
			toServerResponseEx(routes.identifier(), routes.microservice(), ex, DELETE_PATH);
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
			toServerResponseEx(routes.identifier(), routes.microservice(), ex, REFERENCE_PATH);
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
			toServerResponseEx(routes.identifier(), routes.microservice(), ex, UPDATE_PATH);
		}

		return output;
	}

	@Override
	public GetMergedMetadatiDTO metadata(final MergedMetadatiRequestDTO request) {

		String endpoint = routes.metadata();
		GetMergedMetadatiDTO output = null;

		log.debug("{} - Executing request: {}", routes.identifier(), endpoint);

		try {
			// Execute request
			ResponseEntity<GetMergedMetadatiDTO> response = client.exchange(
				endpoint,
				PUT,
				new HttpEntity<>(request),
				GetMergedMetadatiDTO.class
			);
			// Retrieve body
			output = response.getBody();
		} catch (RestClientResponseException ex) {
			toServerResponseEx(routes.identifier(), routes.microservice(), ex, METADATA_PATH);
		}

		return output;
	}
}
