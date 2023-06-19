/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.audit.exclusions;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.audit.exclusions.ActuatorExclusion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

import static it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants.Profile.TEST;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles(TEST)
class ActuatorExclusionTest {

    private final List<String> TEST_EP = Arrays.asList("/v1/status", "/v1/health-ready", "/test", "/not-found");

    @Autowired
    private WebEndpointProperties endpoints;
    @Autowired
    private ActuatorExclusion exclusion;

    @Test
    void verify() {
        for (String path : getActuatorsPath()) {
            assertTrue(exclusion.verify(path, null));
        }
        for (String path : TEST_EP) {
            assertFalse(exclusion.verify(path, null));
        }
    }

    private List<String> getActuatorsPath() {
        List<String> current = new ArrayList<>();
        // Retrieve actuator exposed endpoints
        Set<String> ep = endpoints.getExposure().getInclude();
        // Retrieve actuator base path
        String base = endpoints.getBasePath();
        // Retrieve mapping
        Map<String, String> mapping = endpoints.getPathMapping();
        // Iterate
        for (String endpoint : ep) {
            // Retrieve associated mapping
            // because it may have been re-defined (e.g live -> status ...)
            // If it wasn't overwritten, it will return null therefore we are using the default mapping value
            String mapper = mapping.getOrDefault(endpoint, endpoint);
            // Get actuator path
            String path = UriComponentsBuilder.newInstance().pathSegment(base, mapper).toUriString();
            // Add to list
            current.add(path);
        }
        return current;
    }

}
