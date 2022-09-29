package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

/**
 * @author CPIERASC Microservices URL.
 */
@Configuration
@Getter
public class MicroservicesURLCFG implements Serializable {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -1778677709932831654L;

	/**
	 * Validator host.
	 */
	@Value("${ms.url.gtw-validator-service}")
	private String validatorHost;

	/**
	 * Validator host.
	 */
	@Value("${ms.url.gtw-fhir-mapping-service}")
	private String fhirMappingHost;
	
	/**
	 * Validator host.
	 */
	@Value("${ms.url.gtw-fhir-mapping-engine-service}")
	private String fhirMappingEngineHost;

	/**
	 * EDS Client host.
	 */
	@Value("${ms.url.eds-client-service}")
	private String edsClientHost;

	/**
	 * INI Client host.
	 */
	@Value("${ms.url.ini-client-service}")
	private String iniClientHost;

	@Value("${ms-calls.are-from-govway}")
	private Boolean fromGovway;

	@Value("${ms.url.ana-service}")
	private String anaHost;

	@Value("${ms.ana-service.enable-validation}")
	private Boolean anaEnableValidation;
	
	@Value("${ms.calls.transform-engine}")
	private Boolean callTransformEngine;

}
