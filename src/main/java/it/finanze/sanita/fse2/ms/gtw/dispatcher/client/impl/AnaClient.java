/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IAnaClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.client.AnaReqDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AnaClient implements IAnaClient {

	 
	@Override
	public Boolean callAnaClient(final String codFiscale) {
		log.warn("ATTENZIONE , Si sta chiamando il client mockato di Ana , assicurarsi che sia voluto");
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		AnaReqDTO req = new AnaReqDTO();
		req.setCodiceFiscale(codFiscale);

		return true;
	}

}
