/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.interceptor.LogInterceptor;

@Configuration
@ConditionalOnProperty("ms.dispatcher.audit.enabled")
public class WebCFG implements WebMvcConfigurer {
    
    @Autowired
    private LogInterceptor logInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	registry.addInterceptor(logInterceptor);
    }
} 
 
