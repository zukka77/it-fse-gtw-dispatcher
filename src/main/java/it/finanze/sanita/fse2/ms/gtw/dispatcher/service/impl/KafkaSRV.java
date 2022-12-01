/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.util.Date;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.kafka.KafkaTopicCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.KafkaStatusManagerDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.TSPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.DestinationTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.TipoDocAltoLivEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.PriorityUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;


/**
 * Kafka management service.
 */
@Service
@Slf4j
public class KafkaSRV implements IKafkaSRV {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 987723954716001270L;

	@Autowired
	private transient KafkaTopicCFG kafkaTopicCFG;

	/**
	 * Transactional producer.
	 */
	@Autowired
	@Qualifier("txkafkatemplate")
	private transient KafkaTemplate<String, String> txKafkaTemplate;

	/**
	 * Not transactional producer.
	 */
	@Autowired
	@Qualifier("notxkafkatemplate")
	private transient KafkaTemplate<String, String> notxKafkaTemplate;

	@Autowired
	private transient PriorityUtility priorityUtility;

	@Override
	public RecordMetadata sendMessage(String topic, String key, String value, boolean trans) {
		RecordMetadata out = null;
		ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, value);
		try {
			out = kafkaSend(producerRecord, trans);
		} catch (Exception e) {
			log.error("Send failed.", e);
			throw new BusinessException(e);
		}
		return out;
	}

	@SuppressWarnings("unchecked")
	private RecordMetadata kafkaSend(ProducerRecord<String, String> producerRecord, boolean trans) {
		RecordMetadata out = null;
		Object result = null;

		if (trans) {
			result = txKafkaTemplate.executeInTransaction(t -> {
				try {
					return t.send(producerRecord).get();
				} catch(InterruptedException e) {
					log.error("InterruptedException caught. Interrupting thread...");					
					Thread.currentThread().interrupt(); 
					throw new BusinessException(e); 
				}
				catch (Exception e) {
					throw new BusinessException(e);
				}
			});
		} else {
			notxKafkaTemplate.send(producerRecord);
		}

		if(result != null) {
			SendResult<String,String> sendResult = (SendResult<String, String>) result;
			out = sendResult.getRecordMetadata();	
		}

		return out;
	}

	
	@Override
	public void notifyChannel(
			final String key,
			final String kafkaValue,
			PriorityTypeEnum priorityFromRequest,
			TipoDocAltoLivEnum documentType,
			DestinationTypeEnum destinationType
	) {
		log.debug("Destination: {}", destinationType.name());
		try {
			String destTopic = priorityUtility.computeTopic(priorityFromRequest, destinationType, documentType);
			sendMessage(destTopic, key, kafkaValue,true);
		} catch (Exception e) {
			log.error("Error sending kafka message", e);
			throw new BusinessException(e);
		}
	}

	@Override
	public void sendValidationStatus(final String traceId,final String workflowInstanceId, final EventStatusEnum eventStatus, final String message,
									 final JWTPayloadDTO jwtClaimDTO) {
		sendStatusMessage(traceId,workflowInstanceId, EventTypeEnum.VALIDATION, eventStatus, message, null, jwtClaimDTO, null);
	}

	@Override
	public void sendPublicationStatus(final String traceId,final String workflowInstanceId, final EventStatusEnum eventStatus, final String message,
									  final PublicationCreationReqDTO publicationReq, final JWTPayloadDTO jwtClaimDTO) {

		String identificativoDocumento = null;
		AttivitaClinicaEnum tipoAttivita = null;
		if (publicationReq != null)  {
			if (publicationReq.getIdentificativoDoc()!=null) {
				identificativoDocumento = publicationReq.getIdentificativoDoc();
			}
			if(publicationReq.getTipoAttivitaClinica()!=null) {
				tipoAttivita = publicationReq.getTipoAttivitaClinica();
			}
		}
		sendStatusMessage(traceId,workflowInstanceId, EventTypeEnum.PUBLICATION, eventStatus, message, identificativoDocumento, jwtClaimDTO,tipoAttivita);
	}
	
	@Override
	public void sendReplaceStatus(final String traceId,final String workflowInstanceId, final EventStatusEnum eventStatus, final String message,
									  final PublicationCreationReqDTO publicationReq, final JWTPayloadDTO jwtClaimDTO) {

		String identificativoDocumento = null;
		AttivitaClinicaEnum tipoAttivita = null;
		if (publicationReq != null)  {
			if (publicationReq.getIdentificativoDoc()!=null) {
				identificativoDocumento = publicationReq.getIdentificativoDoc();
			}
			if(publicationReq.getTipoAttivitaClinica()!=null) {
				tipoAttivita = publicationReq.getTipoAttivitaClinica();
			}
		}
		sendStatusMessage(traceId,workflowInstanceId, EventTypeEnum.REPLACE, eventStatus, message, identificativoDocumento, jwtClaimDTO,tipoAttivita);
	}

	@Override
	public void sendDeleteStatus(String traceId, String workflowInstanceId, String idDoc, String message, EventStatusEnum eventStatus, JWTPayloadDTO jwt,
			EventTypeEnum eventType) {
		sendStatusMessage(traceId, workflowInstanceId, eventType, eventStatus, message, idDoc, jwt, AttivitaClinicaEnum.PHR);
	}

	@Override
	public void sendDeleteRequest(String workflowInstanceId, Object request) {
		sendIndexerRetryMessage(workflowInstanceId, sendObjectAsJson(request),kafkaTopicCFG.getDispatcherIndexerRetryDeleteTopic());
	}
	
	@Override
	public void sendUpdateRequest(String workflowInstanceId, Object request) {
		sendIndexerRetryMessage(workflowInstanceId, sendObjectAsJson(request),kafkaTopicCFG.getDispatcherIndexerRetryUpdateTopic());
	}

	@Override
	public void sendFeedingStatus(final String traceId,final String workflowInstanceId, final EventStatusEnum eventStatus, final String message,
								  final TSPublicationCreationReqDTO feedingReq, final JWTPayloadDTO jwtClaimDTO) {

		sendStatusMessage(traceId,workflowInstanceId, EventTypeEnum.FEEDING, eventStatus, message, feedingReq.getIdentificativoDoc(),
				jwtClaimDTO, feedingReq.getTipoAttivitaClinica());
	}
	
	@Override
	public void sendUpdateStatus(String traceId, String workflowInstanceId, String idDoc, EventStatusEnum eventStatus, JWTPayloadDTO jwt,
			String message,EventTypeEnum event) {
		sendStatusMessage(traceId, workflowInstanceId,event , eventStatus, message, idDoc, jwt,null);
	}

	private String sendObjectAsJson(Object o) {
		String json;
		// Try to deserialize message
		try {
			json = new ObjectMapper().writeValueAsString(o);
		} catch (JsonProcessingException e) {
			json = "Unable to deserialize content request";
		}
		return json;
	}

	private void sendIndexerRetryMessage(final String workflowInstanceId, final String json,
			final String topic) {
		sendMessage(topic, workflowInstanceId, json, true);
	}

	private void sendStatusMessage(final String traceId,final String workflowInstanceId, final EventTypeEnum eventType,
								   final EventStatusEnum eventStatus, final String message, final String documentId,
								   final JWTPayloadDTO jwtClaimDTO, AttivitaClinicaEnum tipoAttivita) {
		try {
			KafkaStatusManagerDTO statusManagerMessage = KafkaStatusManagerDTO.builder().
					issuer(jwtClaimDTO != null ? jwtClaimDTO.getIss() : Constants.App.JWT_MISSING_ISSUER_PLACEHOLDER).
					traceId(traceId).
					eventType(eventType).
					eventDate(new Date()).
					eventStatus(eventStatus).
					message(message).
					identificativoDocumento(documentId).
					tipoAttivita(tipoAttivita).
					subject(jwtClaimDTO != null ? jwtClaimDTO.getSub() : null).
					organizzazione(jwtClaimDTO != null ? jwtClaimDTO.getSubject_organization_id() : null).
					build();

			String json = StringUtility.toJSONJackson(statusManagerMessage);
			sendMessage(kafkaTopicCFG.getStatusManagerTopic(), workflowInstanceId, json, true);
		} catch(Exception ex) {
			log.error("Error while send status message : " , ex);
			throw new BusinessException(ex);
		}
	}

	@Override
	public void sendLoggerStatus(final String log) {
		sendMessage(kafkaTopicCFG.getLogTopic(), "KEY", log, true);
		
	}
}
