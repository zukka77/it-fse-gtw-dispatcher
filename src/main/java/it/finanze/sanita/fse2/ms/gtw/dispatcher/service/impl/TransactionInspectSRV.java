package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IStatusCheckClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.ITransactionInspectSRV;

@Service
public class TransactionInspectSRV implements ITransactionInspectSRV {

	@Autowired
	private IStatusCheckClient statusCheckClient;
	
	@Override
	public void callSearchLastEventByWorkflowInstanceId() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void callSearchEventByWorkflowInstanceId() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void callSearchEventByTraceId() {
		// TODO Auto-generated method stub
		
	}

}
