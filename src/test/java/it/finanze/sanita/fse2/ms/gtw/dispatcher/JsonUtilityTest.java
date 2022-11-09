/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.MetadataPatientDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.JsonUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
class JsonUtilityTest {
    @Test
    @DisplayName("jsonToObject malformedInput")
    void testJsonToObjectMalformedInput() {
        String malformedInput = "{\"input\":}";
        assertEquals(null, JsonUtility.jsonToObject(malformedInput, MetadataPatientDTO.class));
    }

    @Test
    @DisplayName("jsonToObject OK")
    void testJsonToObjectOk() {
        String input = "{\"identificativo\":\"stub\",\"flagPresaInCarico\":false}";
        Map<String, Object> map = new HashMap<>();
        map.put("identificativo", "stub");
        map.put("flagPresaInCarico", false);
        assertEquals(map, JsonUtility.jsonToObject(input, Map.class));
    }

    @Test
    @DisplayName("objectToJson malformedInput")
    void testObjectToJsonMalformedInput() {
        Map<String, Object> input = new HashMap<>();
        input.put(null, null);
        assertEquals("", JsonUtility.objectToJson(input));
    }

    @Test
    @DisplayName("objectToJson OK")
    void testObjectToJsonOK() {
        Map<String, Object> input = new HashMap<>();
        input.put("key", "value");
        assertEquals("{\"key\":\"value\"}", JsonUtility.objectToJson(input));
    }
}
