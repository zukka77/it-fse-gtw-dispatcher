package it.finanze.sanita.fse2.ms.gtw.dispatcher.utility;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;

@Component
public class ErrorUtility {

    @Value("${ms.dispatcher.errors.blocking-list}")
    private String blockingErrorList;

    @Value("${ms.dispatcher.errors.non-blocking-list}")
    private String nonBlockingErrorList;

    /**
     * Compute error status basing on error type
     * @param errorType
     * @return
     */
    public EventStatusEnum computeErrorStatus(String errorType) {
        if (this.blockingErrorList.contains(errorType)) {
            return EventStatusEnum.BLOCKING_ERROR;
        } else if (this.nonBlockingErrorList.contains(errorType)) {
            return EventStatusEnum.NON_BLOCKING_ERROR;
        } else {
            return EventStatusEnum.BLOCKING_ERROR;
        }
    }
}
