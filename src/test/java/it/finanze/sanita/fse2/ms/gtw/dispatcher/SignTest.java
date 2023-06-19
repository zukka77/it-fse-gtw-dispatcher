/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.SignatureValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.SignVerificationModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.helper.SignerHelper;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.FileUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles(Constants.Profile.TEST)
class SignTest {
    
    @Test
    @Disabled
    void validSignatureTest() throws Exception {
        byte[] fileByte = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");

        File file = File.createTempFile("temp", "signer");
        file.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(fileByte);
		}

        assertTrue(SignerHelper.isSigned(file), "The file should result as signed");
        SignatureValidationDTO result = SignerHelper.validate(fileByte, SignVerificationModeEnum.SIGNING_DAY);

        assertEquals(1, result.getNumSignatures(), "The file should be signed once");
        assertTrue(result.getStatus(), "The file should be valid");
    }

    @Test
    @Disabled
    void invalidSignatureTest() throws Exception {
        byte[] fileByte = FileUtility.getFileFromInternalResources("Files/attachment/LAB_OK.pdf");

        File file = File.createTempFile("temp", "signer");
        file.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(fileByte);
		}

        assertFalse(SignerHelper.isSigned(file), "The file should result as not signed");
        SignatureValidationDTO result = SignerHelper.validate(fileByte, SignVerificationModeEnum.SIGNING_DAY);

        assertEquals(0, result.getNumSignatures(), "The file should be not signed");
        assertFalse(result.getStatus(), "The file should be invalid");
    }
}
