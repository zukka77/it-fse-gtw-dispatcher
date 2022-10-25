/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IIniClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.exceptions.RecordNotFoundException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.DeleteRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.IniMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniTraceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.INIErrorEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * @author AndreaPerquoti
 *
 */
@Slf4j
@Component
public class IniClient extends AbstractClient implements IIniClient {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 8054486322408383036L;

	@Autowired
	private transient RestTemplate restTemplate;

	@Autowired
	private transient ProfileUtility profileUtility;

	@Autowired
	private MicroservicesURLCFG msUrlCFG;

	@Override
	public IniTraceResponseDTO delete(final DeleteRequestDTO iniReq) {
		IniTraceResponseDTO output = null;

		try {
			log.debug("INI Client - Calling Ini to execute delete operation");

			// Build headers.
			HttpEntity<Object> entity = new HttpEntity<>(iniReq, null);
			
			// Build endpoint e call.
			String endpoint = msUrlCFG.getIniClientHost() + Constants.Client.Ini.DELETE_PATH;
			ResponseEntity<IniTraceResponseDTO> restExchange = restTemplate.exchange(endpoint, HttpMethod.DELETE, entity, IniTraceResponseDTO.class);

			// Gestione response
			output = restExchange.getBody();
			this.checkResponseFromIni(output);
		} catch(RecordNotFoundException | BusinessException e0) {
			throw e0;
		} catch (HttpClientErrorException e1) {
			errorHandler(e1, "/delete");
		} catch (Exception e) {
			log.error("Errore durante l'invocazione di INI dell'API delete(). ", e);
			throw e;
		}

		return output;
	}
	
	@Override
	public IniTraceResponseDTO updateMetadati(IniMetadataUpdateReqDTO iniReq) {
		IniTraceResponseDTO out = null;
		try {
			log.debug("INI Client - Calling INI to execute update metadati :{}", iniReq.getIdDoc());

			// Build headers.
			HttpEntity<Object> entity = new HttpEntity<>(iniReq, null);

			// Build endpoint e call.
			String endpoint = msUrlCFG.getIniClientHost() + Constants.Client.Ini.UPDATE_PATH;
			ResponseEntity<IniTraceResponseDTO> restExchange = restTemplate.exchange(endpoint, HttpMethod.PUT, entity, IniTraceResponseDTO.class);

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
}
