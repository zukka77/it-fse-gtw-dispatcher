/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

/**
 * Validation Event properties.
 */
@Configuration
@Getter
public class ValidationCFG {
    
	@Value("${validation.allow-special-fiscal-code}")
	private Boolean allowSpecialFiscalCodes;
	
	@Value("${days.allow-publish-after-validation}")
	private Integer daysAllowToPublishAfterValidation;
    
	@Value("${accreditamento.hash-allowed}")
	private List<String> hashAllowed;
}
