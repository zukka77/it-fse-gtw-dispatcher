/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.base.AbstractClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.IniClientRoutes;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.INIErrorEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.ProfileUtility;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IIniClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.exceptions.RecordNotFoundException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants.Client.Ini;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.DeleteRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.IniMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.IniReferenceRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.MergedMetadatiRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.GetMergedMetadatiDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniReferenceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniTraceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.GetMergedMetadatiResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.http.HttpMethod.*;

@Slf4j
@Component
public class IniClient extends AbstractClient implements IIniClient {

	@Autowired
	private MicroservicesURLCFG msUrlCFG;

	@Autowired
	private ProfileUtility profileUtility;

	@Autowired
	private RestTemplate client;

	@Autowired
	private IniClientRoutes routes;

	@Override
	public IniTraceResponseDTO delete(final DeleteRequestDTO request) {

		log.debug("{} - Executing request: {}", routes.identifier(), routes.delete());

		IniTraceResponseDTO output = null;

		try {
			// Execute request
			ResponseEntity<IniTraceResponseDTO> response = client.exchange(
				routes.delete(),
				DELETE,
				new HttpEntity<>(request),
				IniTraceResponseDTO.class
			);
			// Retrieve body
			output = response.getBody();
		} catch (RestClientResponseException ex) {
			toServerResponseEx(routes.identifier(), ex, routes.delete());
		}

		if(output == null) log.debug("{} - Request failure: {}", routes.identifier(), routes.delete());

		return output;
	}
	
	@Override
	public IniTraceResponseDTO updateMetadati(IniMetadataUpdateReqDTO iniReq) {
		IniTraceResponseDTO out = null;
		try {
			log.debug("INI Client - Calling INI to execute update metadati");

			// Build headers.
			HttpEntity<Object> entity = new HttpEntity<>(iniReq, null);

			// Build endpoint e call.
			String endpoint = msUrlCFG.getIniClientHost() + Ini.UPDATE_PATH;
			ResponseEntity<IniTraceResponseDTO> restExchange = client.exchange(endpoint, PUT, entity, IniTraceResponseDTO.class);

			// Gestione response
			out = restExchange.getBody();
			this.checkResponseFromIni(out);
		} catch(RecordNotFoundException | BusinessException e0) {
			throw e0;
		} catch (HttpStatusCodeException e2) {
			errorHandler(e2, "/ini-update");
		} catch (Exception e) {
			log.error("Errore durante l'invocazione dell' API update(). ", e);
			throw e;
		}

		return out;
	}

	@Override
	public IniReferenceResponseDTO getReference(IniReferenceRequestDTO iniReq) {
		IniReferenceResponseDTO out = null;
		try {
			log.debug("INI Client - Calling INI to retrieve reference :{}", iniReq.getIdDoc());

			// Build headers.
			HttpEntity<Object> entity = new HttpEntity<>(iniReq.getToken(), null);

			// Build endpoint e call.
			String endpoint = msUrlCFG.getIniClientHost() + Ini.REFERENCE_PATH.replace("{idDoc}", iniReq.getIdDoc());
			ResponseEntity<IniReferenceResponseDTO> restExchange = client.exchange(endpoint, HttpMethod.POST, entity, IniReferenceResponseDTO.class);

			// Gestione response
			out = restExchange.getBody();
		} catch (HttpStatusCodeException e2) {
			errorHandler(e2, "/ini-reference");
		} catch (Exception e) {
			log.error("Errore durante l'invocazione dell' API reference(). ", e);
			throw e;
		}

		return out;
	}

	/**
	 * Check response from INI
	 * @param out
	 */
	private void checkResponseFromIni(IniTraceResponseDTO out) {
		if (!profileUtility.isDevOrDockerProfile() && out != null && Boolean.FALSE.equals(out.getEsito())) {
			if (!StringUtils.isEmpty(out.getErrorMessage())) {
				boolean notFound = out.getErrorMessage().equals(INIErrorEnum.RECORD_NOT_FOUND.toString());
				if (notFound) {
					throw new RecordNotFoundException(out.getErrorMessage());
				} else {
					throw new BusinessException(out.getErrorMessage());
				}
			} else {
				throw new BusinessException("INI operation(): Generic error");
			}
		}
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
