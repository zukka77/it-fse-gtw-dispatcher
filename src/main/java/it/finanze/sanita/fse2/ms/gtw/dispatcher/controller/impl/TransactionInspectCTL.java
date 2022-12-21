/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.ITransactionInspectCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TransactionInspectResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.ITransactionInspectSRV;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
public class TransactionInspectCTL extends AbstractCTL implements ITransactionInspectCTL {

	@Autowired
	private ITransactionInspectSRV transactionInspectSRV;

	@Override
	public TransactionInspectResDTO getEvents(String workflowInstanceId, HttpServletRequest request) {
		log.info("[START] {}() with arguments {}={}", "getEvents", "wif", workflowInstanceId);
		TransactionInspectResDTO res = transactionInspectSRV.callSearchEventByWorkflowInstanceId(workflowInstanceId);
		log.info("[EXIT] {}() with arguments {}={}, {}={}", "getEvents", "reqTraceId", res.getTraceID(), "wif", workflowInstanceId);
		return res;
	}
 
	@Override
	public TransactionInspectResDTO getEventsByTraceId(String traceId, HttpServletRequest request) {
		log.info("[START] {}() with arguments {}={}", "getEventsByTraceId", "traceId", traceId);
		TransactionInspectResDTO res = transactionInspectSRV.callSearchEventByTraceId(traceId);
		log.info("[EXIT] {}() with arguments {}={}", "getEventsByTraceId", "traceId", traceId);
		return res;
	}
}
