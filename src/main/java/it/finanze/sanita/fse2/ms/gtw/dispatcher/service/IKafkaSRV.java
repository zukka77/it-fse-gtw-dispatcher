/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import org.apache.kafka.clients.producer.RecordMetadata;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.DestinationTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.TipoDocAltoLivEnum;

/**
 * Interface for service used to handle kafka communications
 */
public interface IKafkaSRV {

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
	 * Send message to either indexer or publisher over kafka topic, choosing priority
	 * @param key
	 * @param value
	 * @param priorityType
	 * @param documentType
	 * @param destinationTypeEnum
	 */
	void notifyChannel(String key, String value, PriorityTypeEnum priorityType, TipoDocAltoLivEnum documentType, DestinationTypeEnum destinationTypeEnum);
	
	void sendValidationStatus(String traceId, String workflowInstanceId, EventStatusEnum eventStatus, String message,JWTPayloadDTO jwtClaimDTO); 

	void sendPublicationStatus(String traceId, String workflowInstanceId, EventStatusEnum eventStatus, String message, PublicationCreationReqDTO publicationReq, JWTPayloadDTO jwtClaimDTO);

	void sendReplaceStatus(String traceId,String workflowInstanceId, EventStatusEnum eventStatus, String message, PublicationCreationReqDTO publicationReq, JWTPayloadDTO jwtClaimDTO);
	void sendDeleteStatus(String traceId, String workflowInstanceId, String idDoc, String message, EventStatusEnum eventStatus, JWTPayloadDTO jwt, EventTypeEnum eventType);
	void sendDeleteRequest(String workflowInstanceId, Object request);
	
	void sendUpdateStatus(String traceId, String workflowInstanceId, String idDoc, EventStatusEnum eventStatus, JWTPayloadDTO jwt, String message,EventTypeEnum event);

	void sendUpdateRequest(String workflowInstanceId, Object request);

}
