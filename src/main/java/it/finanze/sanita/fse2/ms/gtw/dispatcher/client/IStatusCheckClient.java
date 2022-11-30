package it.finanze.sanita.fse2.ms.gtw.dispatcher.client;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TransactionInspectResDTO;

public interface IStatusCheckClient {

	TransactionInspectResDTO callSearchEventByTraceId(String traceId);
	
	TransactionInspectResDTO callSearchEventByWorkflowInstanceId(String workflowInstanceId);
}
