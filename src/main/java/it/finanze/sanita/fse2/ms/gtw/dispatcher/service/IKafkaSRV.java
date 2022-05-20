package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import java.io.Serializable;

import org.apache.kafka.clients.producer.RecordMetadata;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ValidationResultEnum;

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
	 * Send validation event message to be sent over kafka
	 * @param validationReq
	 * @param validationResult
	 * @param isHistoricalDoc
	 */
	void notifyValidationEvent(ValidationCDAReqDTO json, ValidationResultEnum validationResult, boolean isHistoricalDoc, boolean isTSFeeding, String transactionID); 
	
	/**
	 * Send validation event message to be sent over kafka
	 * @param publicationReq
	 * @param publicationResult
	 * @param isHistoricalDoc
	 * @param isTSFeeding
	 */
	void notifyPublicationEvent(PublicationCreationReqDTO publicationReq, PublicationResultEnum publicationResult, boolean isHistoricalDoc, boolean isTSFeeding);
	
	void notifyAfterSaveMapping(String transactionId);
	
}
