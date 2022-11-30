package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IStatusCheckClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TransactionInspectResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.ITransactionInspectSRV;

@Service
public class TransactionInspectSRV implements ITransactionInspectSRV {

	@Autowired
	private IStatusCheckClient statusCheckClient;
	 

	@Override
	public TransactionInspectResDTO callSearchEventByWorkflowInstanceId(final String workflowInstanceId) {
		return statusCheckClient.callSearchEventByWorkflowInstanceId(workflowInstanceId);
	}

	@Override
	public TransactionInspectResDTO callSearchEventByTraceId(final String traceId) {
		return statusCheckClient.callSearchEventByTraceId(traceId);
	}

}
