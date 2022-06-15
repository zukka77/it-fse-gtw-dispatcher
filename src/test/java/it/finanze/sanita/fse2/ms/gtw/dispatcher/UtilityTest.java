package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTHeaderDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.UIDModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CfUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.FileUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;

/**
 * Junit class for utility methods.
 * 
 * @author Simone Lungarella
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
public class UtilityTest {

    @Test
    @DisplayName("Fiscal code check test")
    void fiscalCodeTest () {

        final String fiscalCode16 = "RSSMRA72H26F941L";
        final String fiscalCode17 = "RSSMRA72H26F941LA"; // Should drop last char
        final String fiscalCode11 = "RSSMRA72H26";
        final String fiscalCodeEni = "ENI1234567891234";
        final String fiscalCodeStp = "STP1234567891234";
        
        assertEquals(CfUtility.CF_OK_16, CfUtility.validaCF(fiscalCode16));
        assertEquals(CfUtility.CF_OK_16, CfUtility.validaCF(fiscalCode17));
        assertFalse(CfUtility.CF_OK_11 == CfUtility.validaCF(fiscalCode11));
        assertEquals(CfUtility.CF_ENI_OK, CfUtility.validaCF(fiscalCodeEni));
        assertEquals(CfUtility.CF_STP_OK, CfUtility.validaCF(fiscalCodeStp));
        
        final String fiscalCodeShort = "RSSMRA72H26F941";
        final String fiscalCodeLong = "RSSMRA72H26F941LAA";
        final String fiscalCodeImproper = "RSSMR172H26F941L";
        final String fcImproperEni = "ENI123456789123A";
        final String fcImproperStp = "STP123456789123A";

        assertEquals(0, CfUtility.validaCF(fiscalCodeShort));
        assertEquals(0, CfUtility.validaCF(fiscalCodeLong));
        assertEquals(0, CfUtility.validaCF(fiscalCodeImproper));
        assertEquals(0, CfUtility.validaCF(fcImproperEni));
        assertEquals(0, CfUtility.validaCF(fcImproperStp));
    }

    @ParameterizedTest
    @DisplayName("Transaction UID generation test")
    @ValueSource(ints = { 1, 2, 3, 4})
    void transactionIdTest(final int idMode) {

        String uidGenerated = StringUtility.generateTransactionUID(UIDModeEnum.get(idMode));
        assertFalse(StringUtility.isNullOrEmpty(uidGenerated));
    }

    @Test
    @DisplayName("Mixed utility")
    void genericUtility() {
        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");
        assertNotNull(StringUtility.encodeBase64(pdfAttachment));
        assertNotNull(StringUtility.encodeHex(pdfAttachment));
        assertNotNull(StringUtility.encodeSHA256(pdfAttachment));
        assertNotNull(StringUtility.encodeSHA256B64(new String(pdfAttachment)));
        assertNotNull(StringUtility.encodeSHA256Hex(new String(pdfAttachment)));

        JWTHeaderDTO head = new JWTHeaderDTO("alg", "typ", "kid", "x5c");
        assertDoesNotThrow(() -> StringUtility.fromJSON(StringUtility.toJSON(head), JWTHeaderDTO.class));
    }
}
