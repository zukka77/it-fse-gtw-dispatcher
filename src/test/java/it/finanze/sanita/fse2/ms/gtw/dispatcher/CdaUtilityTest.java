/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility.getWorkflowInstanceId;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility.isValidMasterId;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class CdaUtilityTest {
    @Test
    @DisplayName("CdaUtility - Error getWorkflowInstanceId")
    void getWorkflowInstanceIdErrorTest() {
        Document document = new Document("");
        assertThrows(ValidationException.class, () -> getWorkflowInstanceId(document));
    }

    @Test
    @DisplayName("CdaUtility - MasterId validation")
    void isMasterIdValid() {
        assertFalse(isValidMasterId(null));
        assertFalse(isValidMasterId(""));
        assertFalse(isValidMasterId("^"));
        assertFalse(isValidMasterId("^ab"));
        assertFalse(isValidMasterId("ab^"));
        assertFalse(isValidMasterId("ab^^cd"));
        assertFalse(isValidMasterId("  ^  "));
        assertFalse(isValidMasterId("^  "));
        assertFalse(isValidMasterId("  ^"));
        assertFalse(isValidMasterId("  "));
        assertFalse(isValidMasterId("^^^^"));
        assertFalse(isValidMasterId("  ^^  ^^"));
        assertTrue(isValidMasterId("ab^cd"));
        assertTrue(isValidMasterId("abcd"));
        assertTrue(isValidMasterId("2.16.840.4^UAT_GTW_ID162"));
    }
}
