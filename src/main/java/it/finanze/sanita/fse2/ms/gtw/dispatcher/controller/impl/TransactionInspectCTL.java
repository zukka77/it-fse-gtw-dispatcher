/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.ITransactionInspectCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LastTransactionResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TransactionInspectResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.ITransactionInspectSRV;
import lombok.extern.slf4j.Slf4j;

@RestController
public class TransactionInspectCTL extends AbstractCTL implements ITransactionInspectCTL {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -745041092971509373L;
	
	@Autowired
	private ITransactionInspectSRV transactionInspectSRV;

	@Override
	public TransactionInspectResDTO getEvents(String workflowInstanceId, HttpServletRequest request) {
		return transactionInspectSRV.callSearchEventByWorkflowInstanceId(workflowInstanceId);
	}

 
	@Override
	public TransactionInspectResDTO getEventsByTraceId(String traceId, HttpServletRequest request) {
		return transactionInspectSRV.callSearchEventByTraceId(traceId);
	}
}
