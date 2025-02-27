/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
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
	void sendValidationStatus(String traceId,String workflowInstanceId, EventStatusEnum eventStatus, String message,
			 JWTPayloadDTO jwtClaimDTO, EventTypeEnum eventTypeEnum);
	void sendPublicationStatus(String traceId, String workflowInstanceId, EventStatusEnum eventStatus, String message, PublicationCreationReqDTO publicationReq, JWTPayloadDTO jwtClaimDTO);

	void sendReplaceStatus(String traceId,String workflowInstanceId, EventStatusEnum eventStatus, String message, PublicationCreationReqDTO publicationReq, JWTPayloadDTO jwtClaimDTO);
	void sendDeleteStatus(String traceId, String workflowInstanceId, String idDoc, String message, EventStatusEnum eventStatus, JWTPayloadDTO jwt, EventTypeEnum eventType);
	void sendDeleteRequest(String workflowInstanceId, Object request);
	
	void sendUpdateStatus(String traceId, String workflowInstanceId, String idDoc, EventStatusEnum eventStatus, JWTPayloadDTO jwt, String message,EventTypeEnum event);

	void sendUpdateRequest(String workflowInstanceId, Object request);

}
