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
	 * Topic.
	 */
	@Value("${kafka.dispatcher-statusmanager.topic}")
	private String dispatcherStatusManagerTopic;
	
	/**
	 * Topic indexer.
	 */
	@Value("${kafka.dispatcher-indexer.topic}")
	private String dispatcherIndexerTopic;
}
