package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

/**
 *  @author CPIERASC
 *  Microservices URL.
 */
@Configuration
@Getter
public class MicroservicesURLCFG {

    /** 
     *  Validator host.
     */
	@Value("${ms.url.gtw-validator-service}")
	private String validatorHost;
	
	/** 
     *  Validator host.
     */
	@Value("${ms.url.gtw-fhir-mapping-service}")
	private String fhirMappingHost;
}
