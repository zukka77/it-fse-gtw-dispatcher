package it.finanze.sanita.fse2.ms.gtw.dispatcher.config.kafka;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.ProfileUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

import javax.annotation.PostConstruct;

/**
 *	@author vincenzoingenito
 *
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
	 * Topic publisher.
	 */
	@Value("${kafka.dispatcher-publisher.base-topic}")
	private String dispatcherPublisherTopic;
	
	/**
	 * Log publisher.
	 */
	@Value("${kafka.log.base-topic}")
	private String logTopic;

	@PostConstruct
	public void afterInit() {
		if (profileUtility.isTestProfile()) {
			this.statusManagerTopic = Constants.Profile.TEST_PREFIX + this.statusManagerTopic;
			this.dispatcherIndexerTopic = Constants.Profile.TEST_PREFIX + this.dispatcherIndexerTopic;
			this.dispatcherPublisherTopic = Constants.Profile.TEST_PREFIX + this.dispatcherPublisherTopic;
		}
	}
}
