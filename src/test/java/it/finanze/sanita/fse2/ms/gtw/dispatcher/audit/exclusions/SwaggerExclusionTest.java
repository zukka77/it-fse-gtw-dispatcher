/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.audit.exclusions;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.audit.exclusions.SwaggerExclusion;
import org.junit.jupiter.api.Test;
import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants.Profile.TEST;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles(TEST)
class SwaggerExclusionTest {

    private final List<String> TEST_EP = Arrays.asList("/v1/status", "/v1/health-ready", "/test", "/not-found");

    @Autowired
    private SwaggerUiConfigProperties swagger;

    @Autowired
    private SpringDocConfigProperties api;
    @Autowired
    private SwaggerExclusion exclusion;

    @Test
    void verify() {
        for (String path : getSwaggerPath()) {
            assertTrue(exclusion.verify(path, null));
        }
        for (String path : TEST_EP) {
            assertFalse(exclusion.verify(path, null));
        }
    }

    private List<String> getSwaggerPath() {
        return Arrays.asList(swagger.getPath(), api.getApiDocs().getPath());
    }

}
