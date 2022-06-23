package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.interceptor.LogInterceptor;

/**
 * 
 * @author vincenzoingenito
 *
 */
@Configuration
public class WebCFG implements WebMvcConfigurer {
    
    @Autowired
    private LogInterceptor logInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	registry.addInterceptor(logInterceptor);
    }
} 
 
