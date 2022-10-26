/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.ProfileUtility;


@Configuration
public class CollectionNaming {

    @Autowired
    private ProfileUtility profileUtility;

    @Bean("iniEdsInvocationBean")
    public String getIniEdsInvocationCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.Collections.INI_EDS_INVOCATION;
        }
        return Constants.Collections.INI_EDS_INVOCATION;
    }
    
    @Bean("validatedDocumentsBean")
    public String getValidatedDocuments() {
    	if(profileUtility.isTestProfile()) {
    		return Constants.Profile.TEST_PREFIX + Constants.ComponentScan.Collections.VALIDATED_DOCUMENTS; 
    	} 
    	return Constants.ComponentScan.Collections.VALIDATED_DOCUMENTS; 
    }
}
