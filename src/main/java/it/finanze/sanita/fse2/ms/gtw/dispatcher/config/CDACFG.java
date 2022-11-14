/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

/**
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

}
