package it.finanze.sanita.fse2.ms.gtw.dispatcher.client;

public interface IStatusCheckClient {

	void callSearchLastEventByWorkflowInstanceId();

	public void callSearchEventByWorkflowInstanceId();

	public void callSearchEventByTraceId();
}
