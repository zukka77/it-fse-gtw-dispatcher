package it.finanze.sanita.fse2.ms.gtw.dispatcher.client;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LastTransactionResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TransactionInspectResDTO;

public interface IStatusCheckClient {

	LastTransactionResponseDTO callSearchLastEventByWorkflowInstanceId();
	
	TransactionInspectResDTO callSearchEventByTraceId();
	
	TransactionInspectResDTO callSearchEventByWorkflowInstanceId();
}
