/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.base.AbstractClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.response.WhoIsResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Implementation of gtw-config Client.
 */
@Slf4j
@Component
public class ConfigClient extends AbstractClient implements IConfigClient {

	@Autowired
	private MicroservicesURLCFG msUrlCFG;
	
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ProfileUtility profileUtility;

    @Override
    public String getGatewayName() {
        String gatewayName = null;
        try {
            log.debug("Config Client - Calling Config Client to get Gateway Name");
            final String endpoint = msUrlCFG.getConfigHost() + Constants.Client.Config.WHOIS_PATH;

            final boolean isTestEnvironment = profileUtility.isDevOrDockerProfile() || profileUtility.isTestProfile();
            
            // Check if the endpoint is reachable
            if (isTestEnvironment && !isReachable()) {
                log.warn("Config Client - Config Client is not reachable, mocking for testing purpose");
                return Constants.Client.Config.MOCKED_GATEWAY_NAME;
            }

            final ResponseEntity<WhoIsResponseDTO> response = restTemplate.getForEntity(endpoint,
                    WhoIsResponseDTO.class);

            WhoIsResponseDTO body = response.getBody();
            
            if(body!=null) {
            	if (response.getStatusCode().is2xxSuccessful()) {
                    gatewayName = body.getGatewayName();
                } else {
                    log.error("Config Client - Error calling Config Client to get Gateway Name");
                    throw new BusinessException("The Config Client has returned an error");
                }
            } else {
            	throw new BusinessException("The Config Client has returned an error - The body is null");
            }            
        } catch (HttpStatusCodeException clientException) {
            errorHandler("config", clientException, "/config/whois");
        } catch (Exception e) {
            log.error("Error encountered while retrieving Gateway name", e);
            throw e;
        }
        return gatewayName;
    }

    @Override
    public String getEDSStrategy() {
        String output = ""; //TODO - Set with default strategy

        if(isReachable()) {
            String endpoint = msUrlCFG.getConfigHost() + "/v1/config-items/props?type=GENERIC&props=eds-strategy";
            output = restTemplate.getForObject(endpoint,String.class);
        }
        return output;
    }

    private boolean isReachable() {
        try {
            final String endpoint = msUrlCFG.getConfigHost() + Constants.Client.Config.STATUS_PATH;
            restTemplate.getForEntity(endpoint, String.class);
            return true;
        } catch (ResourceAccessException clientException) {
            return false;
        }
    }

}
