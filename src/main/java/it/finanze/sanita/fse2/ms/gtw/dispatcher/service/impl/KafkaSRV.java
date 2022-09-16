package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.util.Date;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.kafka.KafkaPropertiesCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.kafka.KafkaTopicCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.KafkaStatusManagerDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.HistoricalPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.HistoricalValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.TSPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.EncryptDecryptUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author vincenzoingenito
 *
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
	private KafkaPropertiesCFG kafkaPropCFG;
	
	@Autowired
	private KafkaTopicCFG kafkaTopicCFG;

	/**
	 * Transactional producer.
	 */
	@Autowired
	@Qualifier("txkafkatemplate")
	private KafkaTemplate<String, String> txKafkaTemplate;
	
	/**
	 * Not transactional producer.
	 */
	@Autowired
	@Qualifier("notxkafkatemplate")
	private KafkaTemplate<String, String> notxKafkaTemplate;
 
	@Override
	public RecordMetadata sendMessage(String topic, String key, String value, boolean trans) {
		RecordMetadata out = null;
		ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value); 
		try { 
			out = kafkaSend(record, trans);
		} catch (Exception e) {
			log.error("Send failed.", e); 
			throw new BusinessException(e);
		}   
		return out;
	} 

	private RecordMetadata kafkaSend(ProducerRecord<String, String> record, boolean trans) {
		RecordMetadata out = null;
		Object result = null;

		if (trans) {  
			result = txKafkaTemplate.executeInTransaction(t -> { 
				try {
					return t.send(record).get();
				} catch (Exception e) {
					throw new BusinessException(e);
				}  
			});  
		} else { 
			notxKafkaTemplate.send(record);
		} 

		if(result != null) {
			SendResult<String,String> sendResult = (SendResult<String,String>) result;
			out = sendResult.getRecordMetadata();
			log.info("Send success.");
		}

		return out;
	}
 
	
	@Override
	public void notifyIndexer(final String transactionId) {
		try {
			String message = EncryptDecryptUtility.encryptObject(kafkaPropCFG.getCrypto(), transactionId);
			sendMessage(kafkaTopicCFG.getDispatcherIndexerTopic(), "validation", message,true);
		} catch (Exception e) {
			log.error("Error sending kafka message", e);
			throw new BusinessException(e);
		}

	}

	@Override
	public void notifyPublisher(final String transactionId) {
		try {
			String message = EncryptDecryptUtility.encryptObject(kafkaPropCFG.getCrypto(), transactionId);
			sendMessage(kafkaTopicCFG.getDispatcherPublisherTopic(), "validation", message,true);
		} catch (Exception e) {
			log.error("Error sending kafka message", e);
			throw new BusinessException(e);
		}

	}
	
	@Override
	public void sendValidationStatus(final String traceId,final String transactionId, final EventStatusEnum eventStatus, final String message, 
		final ValidationCDAReqDTO validationReq, final JWTPayloadDTO jwtClaimDTO) {
		
		sendStatusMessage(traceId,transactionId, EventTypeEnum.VALIDATION, eventStatus, message, null,jwtClaimDTO,null);
	}

	@Override
	public void sendPublicationStatus(final String traceId,final EventStatusEnum eventStatus, final String message, 
		final PublicationCreationReqDTO publicationReq, final JWTPayloadDTO jwtClaimDTO) {
		
		if (publicationReq != null) {
			sendStatusMessage(traceId,publicationReq.getWorkflowInstanceId(), EventTypeEnum.PUBLICATION, eventStatus, message, publicationReq.getIdentificativoDoc(), 
				 jwtClaimDTO,publicationReq.getTipoAttivitaClinica());
		}
	}

	@Override
	public void sendFeedingStatus(final String transactionId, final EventStatusEnum eventStatus, final String message, 
			final TSPublicationCreationReqDTO feedingReq, final JWTPayloadDTO jwtClaimDTO) {
		
		sendStatusMessage(null,transactionId, EventTypeEnum.FEEDING, eventStatus, message, feedingReq.getIdentificativoDoc(), 
			jwtClaimDTO, feedingReq.getTipoAttivitaClinica());
	}

	@Override
	public void sendHistoricalValidationStatus(String transactionId, EventStatusEnum eventStatus, String message,
			HistoricalValidationCDAReqDTO historicalValidationReq, JWTPayloadDTO jwtClaimDTO) {
		
		sendStatusMessage(null,transactionId, EventTypeEnum.HISTORICAL_VALIDATION, eventStatus, message, historicalValidationReq.getIdentificativoDoc(), 
			 jwtClaimDTO, historicalValidationReq.getTipoAttivitaClinica());
		
	}

	@Override
	public void sendHistoricalPublicationStatus(EventStatusEnum eventStatus, String message,
			HistoricalPublicationCreationReqDTO historicalPublicationReq, JWTPayloadDTO jwtClaimDTO) {

		if (historicalPublicationReq != null) {
	
			sendStatusMessage(null,historicalPublicationReq.getTransactionID(), EventTypeEnum.HISTORICAL_PUBLICATION, eventStatus, 
					message, historicalPublicationReq.getIdentificativoDoc(), jwtClaimDTO,historicalPublicationReq.getTipoAttivitaClinica());
		}

	}

	private void sendStatusMessage(final String traceId,final String transactionId, final EventTypeEnum eventType,
			final EventStatusEnum eventStatus, final String message, final String documentId,  
			final JWTPayloadDTO jwtClaimDTO, AttivitaClinicaEnum tipoAttivita) {
		try {
			KafkaStatusManagerDTO statusManagerMessage = KafkaStatusManagerDTO.builder().
					issuer(jwtClaimDTO != null ? jwtClaimDTO.getIss() : null).
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
			String cryptoMessage = EncryptDecryptUtility.encryptObject(kafkaPropCFG.getCrypto(), json);
			sendMessage(kafkaTopicCFG.getStatusManagerTopic(), transactionId, cryptoMessage, true);
		} catch(Exception ex) {
			log.error("Error while send status message on indexer : " , ex);
			throw new BusinessException(ex);
		}
	}

	@Override
	public void sendLoggerStatus(final String log, final String operation) {
		sendMessage(kafkaTopicCFG.getLogTopic(), operation, log, true);
	}

}
