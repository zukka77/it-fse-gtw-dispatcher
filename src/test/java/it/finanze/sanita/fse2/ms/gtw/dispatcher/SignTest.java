package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.SignVerificationModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.signer.SignerHelper;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.FileUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
public class SignTest {

    @Test
    @DisplayName("Signature Test")
    void signTest() {
    	byte[] signedFile = FileUtility.getFileFromInternalResources("Files/resource/cert1.pdf");
    	try {
    		assertTrue(SignerHelper.validate(signedFile, SignVerificationModeEnum.SIGNING_DAY).getStatus(), "La certificato di firma doveva essere valido il giorno in cui è stata applicata la firma.");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
