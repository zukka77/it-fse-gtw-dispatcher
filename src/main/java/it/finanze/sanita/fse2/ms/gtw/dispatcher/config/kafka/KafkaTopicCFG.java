package it.finanze.sanita.fse2.ms.gtw.dispatcher.config.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 *	@author vincenzoingenito
 *
 *	Kafka topic configuration.
 */
@Data
@Component
public class KafkaTopicCFG {

	/**
	 * Topic status manager.
	 */
	@Value("${kafka.statusmanager.topic}")
	private String statusManagerTopic;
	
	/**
	 * Topic indexer.
	 */
	@Value("${kafka.dispatcher-indexer.topic}")
	private String dispatcherIndexerTopic;

	/**
	 * Topic publisher.
	 */
	@Value("${kafka.dispatcher-publisher.topic}")
	private String dispatcherPublisherTopic;
}
