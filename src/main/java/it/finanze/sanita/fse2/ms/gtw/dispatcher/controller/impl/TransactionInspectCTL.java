/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.ITransactionInspectCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TransactionInspectResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ErrorInstanceEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.ITransactionInspectSRV;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
		
		if(workflowInstanceId.equalsIgnoreCase(Constants.App.MISSING_WORKFLOW_PLACEHOLDER)) {
			ErrorResponseDTO error = new ErrorResponseDTO(getLogTraceInfo());
			error.setType(RestExecutionResultEnum.INVALID_WII.getType());
			error.setDetail(ErrorInstanceEnum.INVALID_ID_WII.getDescription());
			error.setStatus(HttpStatus.BAD_REQUEST.value());
			error.setTitle(RestExecutionResultEnum.INVALID_WII.getTitle());
			error.setInstance(ErrorInstanceEnum.INVALID_ID_WII.getInstance());
			throw new ValidationException(error);
		}
		
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
