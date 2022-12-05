/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.config.kafka;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.ProfileUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

import javax.annotation.PostConstruct;

/**
 *	Kafka topic configuration.
 */
@Data
@Component
public class KafkaTopicCFG {

	@Autowired
	private ProfileUtility profileUtility;

	/**
	 * Topic status manager.
	 */
	@Value("${kafka.statusmanager.topic}")
	private String statusManagerTopic;
	
	/**
	 * Topic indexer.
	 */
	@Value("${kafka.dispatcher-indexer.base-topic}")
	private String dispatcherIndexerTopic;

	/**
	 * Retry indexer topic
	 */
	@Value("${kafka.dispatcher-indexer.delete-retry-topic}")
	private String dispatcherIndexerRetryDeleteTopic;

	/**
	 * Topic publisher.
	 */
	@Value("${kafka.dispatcher-publisher.base-topic}")
	private String dispatcherPublisherTopic;
	
	/**
	 * Log publisher.
	 */
	@Value("${kafka.log.base-topic}")
	private String logTopic;
	
	/**
	 * Retry indexer topic update
	 */
	@Value("${kafka.dispatcher-indexer.update-retry-topic}")
	private String dispatcherIndexerRetryUpdateTopic;
	

	@PostConstruct
	public void afterInit() {
		if (profileUtility.isTestProfile()) {
			statusManagerTopic = Constants.Profile.TEST_PREFIX + statusManagerTopic;
			dispatcherIndexerTopic = Constants.Profile.TEST_PREFIX + dispatcherIndexerTopic;
			dispatcherIndexerRetryDeleteTopic = Constants.Profile.TEST_PREFIX + dispatcherIndexerRetryDeleteTopic;
			dispatcherPublisherTopic = Constants.Profile.TEST_PREFIX + dispatcherPublisherTopic;
			logTopic = Constants.Profile.TEST_PREFIX + logTopic;
			dispatcherIndexerRetryUpdateTopic = Constants.Profile.TEST_PREFIX + dispatcherIndexerRetryUpdateTopic;
		}
	}
}
