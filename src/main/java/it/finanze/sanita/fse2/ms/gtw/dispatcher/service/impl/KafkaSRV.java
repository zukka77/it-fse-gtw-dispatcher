package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.kafka.KafkaPropertiesCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.kafka.KafkaTopicCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.KafkaMessageDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.PublicationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationCDAInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ValidationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.EncryptDecryptUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author vincenzoingenito
 *
 * Kafka management service.
 */
@Service
@Slf4j
public class KafkaSRV implements IKafkaSRV{

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
	public void notifyValidationEvent(ValidationCDAReqDTO json, ValidationResultEnum validationResult,
			boolean isHistoricalDoc, boolean isTSFeeding, String transactionID) {
		try {
			ValidationCDAInfoDTO validationInfo = ValidationCDAInfoDTO.builder().transactionID(transactionID)
					.activity(json.getActivity()).identificativoDoc(json.getIdentificativoDoc())
					.identificativoPaziente(json.getIdentificativoPaziente())
					.identificativoSottomissione(json.getIdentificativoSottomissione()).build();
			KafkaMessageDTO msg = KafkaMessageDTO.builder().validationInfo(validationInfo)
					.validationResult(validationResult).build();
			String message = EncryptDecryptUtility.encryptObject(kafkaPropCFG.getCrypto(), msg);

			String key = "";
			if (isHistoricalDoc) {
				key = "HV";
			} else if(isTSFeeding) {
				key = "TSV";
			} else {
				key = "V";
			}

			sendMessage(kafkaTopicCFG.getDispatcherStatusManagerTopic(), key, message, true);
		} catch (Exception e) {
			log.error("Error sending kafka message", e);
			throw new BusinessException(e);
		}

	}

	@Override
	public void notifyPublicationEvent(PublicationCreationReqDTO publicationReq,
			PublicationResultEnum publicationResult, boolean isHistoricalDoc, boolean isTSFeeding) {

		try {

			PublicationInfoDTO publicationInfo = PublicationInfoDTO.builder()
					.transactionID(publicationReq.getTransactionID())
					.identificativoDoc(publicationReq.getIdentificativoDoc())
					.identificativoPaziente(publicationReq.getIdentificativoPaziente())
					.identificativoSottomissione(publicationReq.getIdentificativoSottomissione())
					.forcePublish(publicationReq.isForcePublish()).build();

			KafkaMessageDTO msg = KafkaMessageDTO.builder().publicationInfo(publicationInfo)
					.publicationResult(publicationResult).build();

			String message = EncryptDecryptUtility.encryptObject(kafkaPropCFG.getCrypto(), msg);

			String key = "";
			if (isHistoricalDoc) {
				key = "HP";
			} else if(isTSFeeding) {
				key = "TSP";
			} else {
				key = "P";
			}

			sendMessage(kafkaTopicCFG.getDispatcherStatusManagerTopic(), key, message, true);

		} catch (Exception e) {
			log.error("Error sending kafka message", e);
			throw new BusinessException(e);
		}

	}
	
	
	@Override
	public void notifyAfterSaveMapping(final String transactionId) {
		try {
			String message = EncryptDecryptUtility.encryptObject(kafkaPropCFG.getCrypto(), transactionId);
			sendMessage(kafkaTopicCFG.getDispatcherIndexerTopic(), "validation", message,true);
		} catch (Exception e) {
			log.error("Error sending kafka message", e);
			throw new BusinessException(e);
		}

	}
}
