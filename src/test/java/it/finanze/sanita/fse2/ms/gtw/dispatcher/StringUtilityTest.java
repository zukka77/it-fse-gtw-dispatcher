/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class StringUtilityTest {
    @Test
    void encodeSHA256ErrorTest() {
        Assertions.assertThrows(BusinessException.class, () -> StringUtility.encodeSHA256(null));
    }

    @Test
    void encodeSHA256B64ErrorTest() {
        Assertions.assertThrows(BusinessException.class, () -> StringUtility.encodeSHA256B64(null));
    }

    @Test
    void encodeSHA256HexErrorTest() {
        Assertions.assertThrows(BusinessException.class, () -> StringUtility.encodeSHA256Hex(null));
    }

    @Test
    void fromJsonJacksonErrorTest() {
        Assertions.assertThrows(ValidationException.class, () -> StringUtility.fromJSONJackson(null, Object.class));
        String input = "{\"input\":\"\"}";
        Assertions.assertThrows(ValidationException.class, () -> StringUtility.fromJSONJackson(input, ValidationInfoDTO.class));
    }

    @Test
    void toJsonJacksonErrorTest() {
        Map<String, Object> map = new HashMap<>();
        map.put(null, "null");
        Assertions.assertThrows(BusinessException.class, () -> StringUtility.toJSONJackson(map));
    }
}
