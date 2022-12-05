/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.config.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 *	Kafka producer properties configuration.
 */
@Data
@Component
public class KafkaProducerPropertiesCFG {
  
	/**
	 * Boostrap server.
	 */
	@Value("${kafka.bootstrap-servers}")
	private String producerBootstrapServers;
	
	/**
	 * Client id.
	 */
	@Value("${kafka.producer.client-id}")
	private String clientId;
	
	/**
	 * Retries.
	 */
	@Value("${kafka.producer.retries}")
	private Integer retries;
	
	/**
	 * Key serializer.
	 */
	@Value("${kafka.producer.key-serializer}")
	private String keySerializer;
	
	/**
	 * Value serializer.
	 */
	@Value("${kafka.producer.value-serializer}")
	private String valueSerializer;
	
	/**
	 * Transactional id.
	 */
	@Value("${kafka.producer.transactional.id}")
	private String transactionalId;  
	
	/**
	 * Idempotence.
	 */
	@Value("${kafka.producer.enable.idempotence}")
	private Boolean idempotence;  
	
	/**
	 * Ack.
	 */
	@Value("${kafka.producer.ack}")
	private String ack;   
	 
}
