package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

/**
 * Validation Event properties.
 */
@Configuration
@Getter
public class ValidationCFG {

    /**
     * Flag to configure validation event persistance policy.
     */
    @Value("${validation.save-error-events-only}")
    private Boolean saveValidationErrorOnly;

    
	@Value("${ms.dispatcher.other-config-prop-cf}")
	private Boolean otherConfigFiscalCode;
    
}
