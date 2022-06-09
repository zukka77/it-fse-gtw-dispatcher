package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import java.io.Serializable;

import org.apache.kafka.clients.producer.RecordMetadata;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.HistoricalPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.HistoricalValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.TSPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;

/**
 * Interface for service used to handle kafka communications
 */
public interface IKafkaSRV extends Serializable {

	/**
	 * Send message over kafka topic
	 * @param topic
	 * @param key
	 * @param value
	 * @param trans
	 * @return
	 */
	RecordMetadata sendMessage(String topic, String key, String value, boolean trans);
 
	/**
	 * Send message to indexer microservice over kafka topic
	 * @param workflowInstanceId
	 */
	void notifyIndexer(String workflowInstanceId);

	/**
	 * Send message to publisher microservice over kafka topic
	 * @param workflowInstanceId
	 */
	void notifyPublisher(String workflowInstanceId);
	
	void sendValidationStatus(String workflowInstanceId, EventStatusEnum eventStatus, String message, ValidationCDAReqDTO validationReq, JWTPayloadDTO jwtClaimDTO);

	void sendPublicationStatus(EventStatusEnum eventStatus, String message, PublicationCreationReqDTO publicationReq, JWTPayloadDTO jwtClaimDTO);

	void sendFeedingStatus(String workflowInstanceId, EventStatusEnum eventStatus, String message, TSPublicationCreationReqDTO publicationReq, JWTPayloadDTO jwtClaimDTO);

	void sendHistoricalValidationStatus(String workflowInstanceId, EventStatusEnum eventStatus, String message, HistoricalValidationCDAReqDTO historicalValidationReq, JWTPayloadDTO jwtClaimDTO);

	void sendHistoricalPublicationStatus(EventStatusEnum eventStatus, String message, HistoricalPublicationCreationReqDTO historicalPublicationReq, JWTPayloadDTO jwtClaimDTO);

}
