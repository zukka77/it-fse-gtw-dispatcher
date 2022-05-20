package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ValidationResultEnum;
import lombok.Builder;
import lombok.Getter;

/**
 * Content of a Kafka message
 */
@Getter
@Builder
public class KafkaMessageDTO extends AbstractDTO {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -2144344497295875698L;

    /**
     * Validation info
     */
    @Schema(description = "Validation info")
    private ValidationCDAInfoDTO validationInfo;

    /**
     * Validation Result
     */
    @Schema(description = "Validation result")
    private ValidationResultEnum validationResult;


    /**
     * Publication info
     */
    @Schema(description = "Publication info")
    private PublicationInfoDTO publicationInfo;

    /**
     * Publication info
     */
    @Schema(description = "Publication result")
    private PublicationResultEnum publicationResult;
    
}
