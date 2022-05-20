package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

/**
 *  @author CPIERASC
 *  Configuration CDA.
 */
@Configuration
@Getter
public class CDACFG {

    /** 
     *  CDA attachment name.
     */
	@Value("${cda.attachment.name}")
	private String cdaAttachmentName;

	/** 
     *  CDA validation ttl.
     */
	@Value("${cda.redis.validation-ttl}")
	private Long validationTTL;
	
}
