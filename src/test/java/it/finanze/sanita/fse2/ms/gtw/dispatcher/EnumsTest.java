/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
class EnumsTest {

    @Test
    @DisplayName("AssettoOrgEnum test")
    void testAssettoOrgEnums() {
        String code = "AD_PSC001";
        String description = "Allergologia";
        assertEquals(code, PracticeSettingCodeEnum.AD_PSC001.getCode());
        assertEquals(description, PracticeSettingCodeEnum.AD_PSC001.getDescription());
    }

    @Test
    @DisplayName("IniActivityEnums test")
    void testIniActivityEnums() {
        String code = "READ";
        String description = "Lettura";
        assertEquals(code, IniActivityEnum.READ.getCode());
        assertEquals(description, IniActivityEnum.READ.getDescription());
    }

    @Test
    @DisplayName("RegionCodeEnum test")
    void testRegionCodeEnums() {
        String code = "160";
        String description = "Regione Puglia";
        assertEquals(code, SubjectOrganizationEnum.REGIONE_PUGLIA.getCode());
        assertEquals(description, SubjectOrganizationEnum.REGIONE_PUGLIA.getDisplay());
    }

    @Test
    @DisplayName("RoleEnum test")
    void testRoleEnums() {
        String code = "DSA";
        String description = "Direttore sanitario";
        assertEquals(code, RoleEnum.DSA.getCode());
        assertEquals(description, RoleEnum.DSA.getDescription());
    }

    @Test
    @DisplayName("SignVerificationModeEnum test")
    void testSignVerificationModeEnums() {
        String code = "T";
        assertEquals(code, SignVerificationModeEnum.TODAY.getCode());
    }

    @Test
    @DisplayName("RawValidationEnum test")
    void testRawValidationEnums() {
        String code = "00";
        String description = "OK";
        assertEquals(code, RawValidationEnum.OK.getCode());
        assertEquals(description, RawValidationEnum.OK.getDescription());
    }

    @Test
    @DisplayName("EventStatusEnum test")
    void testEventStatusEnums() {
        String name = "SUCCESS";
        assertEquals(name, EventStatusEnum.SUCCESS.getName());
    }

    @Test
    @DisplayName("InjectionModeEnum test")
    void testInjectionModeEnums() {
        String code = "A";
        assertEquals(code, InjectionModeEnum.ATTACHMENT.getCode());
    }

    @Test
    @DisplayName("HealthDataFormatEnum test")
    void testHealthDataFormatEnums() {
        String code = "V";
        assertEquals(code, HealthDataFormatEnum.CDA.getCode());
    }

    @Test
    @DisplayName("ResultLogEnum test")
    void testResultLogEnums() {
        String code = "OK";
        String description = "Operazione eseguita con successo";
        assertEquals(code, ResultLogEnum.OK.getCode());
        assertEquals(description, ResultLogEnum.OK.getDescription());
    }

    @Test
    @DisplayName("ActivityEnum test")
    void testActivityEnums() {
        String code = "V";
        assertEquals(code, ActivityEnum.VERIFICA.getCode());
    }

    @Test
    @DisplayName("EventTypeEnum test")
    void testEventTypeEnums() {
        String name = "VALIDATION";
        assertEquals(name, EventTypeEnum.VALIDATION.getName());
    }

    @Test
    @DisplayName("OperationLogEnum test")
    void testOperationLogEnums() {
        String code = "PUB-CDA2";
        String description = "Pubblicazione CDA2";
        assertEquals(description, OperationLogEnum.PUB_CDA2.getDescription());
        assertEquals(code, OperationLogEnum.PUB_CDA2.getCode());
    }

    @Test
    @DisplayName("EventCodeEnum test")
    void testEventCodeEnum() {
        String code = "P99";
        String description = "Oscuramento del documento";
        assertEquals(description, EventCodeEnum.P99.getDescription());
        assertEquals(code, EventCodeEnum.P99.getCode());
    }

    @Test
    @DisplayName("RestExecutionResultEnum test")
    void testRestExecutionResultEnum() {
        String type = "00";
        String title = "Pubblicazione effettuata correttamente.";
        assertNull(RestExecutionResultEnum.OK.getErrorCategory());
        assertEquals(title, RestExecutionResultEnum.OK.getTitle());
        assertEquals(type, RestExecutionResultEnum.OK.getType());

        RawValidationEnum rawResult = RawValidationEnum.OK;
        assertEquals(rawResult.getCode(), RestExecutionResultEnum.fromRawResult(rawResult).getType());
        rawResult = RawValidationEnum.SEMANTIC_ERROR;
        assertEquals(rawResult.getDescription() + ".", RestExecutionResultEnum.fromRawResult(rawResult).getTitle());
        rawResult = RawValidationEnum.SYNTAX_ERROR;
        assertEquals(rawResult.getDescription() + ".", RestExecutionResultEnum.fromRawResult(rawResult).getTitle());
    }
}
