/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IIniClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IStatusCheckClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.IniAuditDto;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.StatusCheckDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TransactionInspectResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.ITransactionInspectSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;

@Service
public class TransactionInspectSRV implements ITransactionInspectSRV {

	@Autowired
	private IStatusCheckClient statusCheckClient;
	 
	@Autowired
	private IIniClient iniClient;
	
	@Autowired
	private IConfigClient configClient;

	@Override
	public TransactionInspectResDTO callSearchEventByWorkflowInstanceId(final String workflowInstanceId) {
		TransactionInspectResDTO out = statusCheckClient.callSearchEventByWorkflowInstanceId(workflowInstanceId); 
		if(true) {
			IniAuditDto auditIniDto = iniClient.callSearchEventByWorkflowInstanceId(workflowInstanceId);
			StatusCheckDTO status = new StatusCheckDTO();
			status.setEventDate(""+auditIniDto.getEventDate());
			Map<String,String> requestAndResponse = new HashMap<>();
			requestAndResponse.put("soapResponse", auditIniDto.getSoapResponse());
			requestAndResponse.put("soapRequest", auditIniDto.getSoapRequest());
			status.setMessage(StringUtility.toJSONJackson(requestAndResponse));
			status.setWorkflowInstanceId(workflowInstanceId);
			out.getTransactionData().add(status);
		}
		
		return out;
		
	}

	@Override
	public TransactionInspectResDTO callSearchEventByTraceId(final String traceId) {
		return statusCheckClient.callSearchEventByTraceId(traceId);
	}

	@Override
	public TransactionInspectResDTO callSearchEventByIdDocumento(final String idDocumento) {
		return statusCheckClient.callSearchEventByTraceId(idDocumento);
	}
}
