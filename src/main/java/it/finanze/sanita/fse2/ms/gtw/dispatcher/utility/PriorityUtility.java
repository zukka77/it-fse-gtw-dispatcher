package it.finanze.sanita.fse2.ms.gtw.dispatcher.utility;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.PriorityDocumentCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.kafka.KafkaTopicCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.DestinationTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.TipoDocAltoLivEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PriorityUtility {

    @Autowired
    private KafkaTopicCFG kafkaTopicCFG;

    @Autowired
    private PriorityDocumentCFG priorityDocumentCFG;

    public String computeTopic(PriorityTypeEnum priorityType, DestinationTypeEnum destinationType, TipoDocAltoLivEnum documentType) {
        String destTopic = "";

        switch (destinationType) {
            case INDEXER:
                destTopic = kafkaTopicCFG.getDispatcherIndexerTopic();
                break;
            case PUBLISHER:
                destTopic = kafkaTopicCFG.getDispatcherPublisherTopic();
                break;
            default:
                throw new BusinessException("Non e' stata specificata una destinazione valida");
        }

        switch (priorityType) {
            case NULL:
                destTopic += this.computePriorityPerDocumentType(documentType);
                break;
            case LOW:
                destTopic += Constants.Misc.LOW_PRIORITY;
                break;
            case HIGH:
                destTopic += Constants.Misc.HIGH_PRIORITY;
                break;
            default:
                throw new BusinessException("Error in computing topic priority for notification");
        }

        return destTopic;
    }

    /**
     * Compute priority per document type
     * @param documentType
     * @return
     */
    private String computePriorityPerDocumentType(TipoDocAltoLivEnum documentType) {
        if (priorityDocumentCFG.getLowPriorityDocuments().contains(documentType.getCode())) {
            log.info("Low priority document: {}", documentType.getCode());
            return Constants.Misc.LOW_PRIORITY;
        } else if (priorityDocumentCFG.getMediumPriorityDocuments().contains(documentType.getCode())) {
            log.info("Medium priority document: {}", documentType.getCode());
            return Constants.Misc.MEDIUM_PRIORITY;
        } else {
            log.info("High priority document: {}", documentType.getCode());
            return Constants.Misc.HIGH_PRIORITY;
        }
    }
}
