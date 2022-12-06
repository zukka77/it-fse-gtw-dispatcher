/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class MicroservicesURLCFG {
 
	/**
	 * Ms gtw-validator host.
	 */
	@Value("${ms.url.gtw-validator-service}")
	private String validatorHost;
	
	/**
	 * Ms gtw-fhir-mapping-engine host.
	 */
	@Value("${ms.url.gtw-fhir-mapping-engine-service}")
	private String fhirMappingEngineHost;

	/**
	 * Ms gtw-eds-client host.
	 */
	@Value("${ms.url.eds-client-service}")
	private String edsClientHost;

	/**
	 * Ms gtw-ini-client host.
	 */
	@Value("${ms.url.ini-client-service}")
	private String iniClientHost;
	
	/**
	 * Ms gtw-status-check host.
	 */
	@Value("${ms.url.status-check-client-service}")
	private String statusCheckClientHost;
	
	/**
	 * Ms gtw-config host.
	 */
    @Value("${ms.url.gtw-config}")
    private String configHost;

	@Value("${ms.url.ana-service}")
	private String anaHost;

	@Value("${ms.ana-service.enable-validation}")
	private Boolean anaEnableValidation;
	
    @Value("${ms-calls.are-from-govway}")
	private Boolean fromGovway;

}
