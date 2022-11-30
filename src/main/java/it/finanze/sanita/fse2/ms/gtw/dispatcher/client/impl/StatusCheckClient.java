package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IStatusCheckClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LastTransactionResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TransactionInspectResDTO;

@Component
public class StatusCheckClient implements IStatusCheckClient{

	@Autowired
	private RestTemplate restTemplate;
	
	@Override
	public LastTransactionResponseDTO callSearchLastEventByWorkflowInstanceId() {
		return null;
	}

	@Override
	public TransactionInspectResDTO callSearchEventByWorkflowInstanceId() {
		return null;
	}

	@Override
	public TransactionInspectResDTO callSearchEventByTraceId() {
		return null;
	}

}
