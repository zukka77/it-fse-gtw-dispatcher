//package it.finanze.sanita.fse2.ms.gtw.dispatcher;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//
//import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
//import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.FileUtility;
//import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles(Constants.Profile.TEST)
//class AccreditamentoTest {
//
//	public static void main(String[] args) {
//		List<String> files = new ArrayList<>();
//		files.add("SIGNED_CERT_VACC1.pdf");
//		files.add("SIGNED_CERT_VACC2.pdf");
//		files.add("SIGNED_LAB1.pdf");
//		files.add("SIGNED_LAB2.pdf");
//		files.add("SIGNED_LDO1.pdf");
//		files.add("SIGNED_LDO2.pdf");
//		files.add("SIGNED_PSS1.pdf");
//		files.add("SIGNED_PSS2.pdf");
//		files.add("SIGNED_RAD1.pdf");
//		files.add("SIGNED_RAD2.pdf");
//		files.add("SIGNED_RSA1.pdf");
//		files.add("SIGNED_RSA2.pdf");
//		files.add("SIGNED_SING_VACC1.pdf");
//		files.add("SIGNED_SING_VACC2.pdf");
//		files.add("SIGNED_VPS1.pdf");
//		files.add("SIGNED_VPS2.pdf");
//		for(String file : files) {
//			byte[] signedCert = FileUtility.getFileFromInternalResources("Files" + File.separator + "accreditamento" + File.separator + file);
//			System.out.println(file + ":" + StringUtility.encodeSHA256(signedCert));
//		}
//	}
//}
