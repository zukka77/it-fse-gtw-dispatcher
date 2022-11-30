package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

public interface ITransactionInspectSRV {

	void callSearchLastEventByWorkflowInstanceId();

	void callSearchEventByWorkflowInstanceId();

	void callSearchEventByTraceId();

}
