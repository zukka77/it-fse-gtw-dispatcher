package it.finanze.sanita.fse2.ms.gtw.dispatcher.signer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.signer.signature.CreateSignaturePades;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PadesSigner {
	
	private KeyStore keystore;
	
	@SuppressWarnings("unused")
	private PadesSigner() {}
	
	public PadesSigner(String keystorePath, String keystorePwd) throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException {
		keystore = KeyStore.getInstance("PKCS12");
        char[] password = keystorePwd.toCharArray();
        try (InputStream is = new FileInputStream(keystorePath))
        {
            keystore.load(is, password);
        }
	}

	public boolean sign(String keystorePwd, File pdfToSign, String signedPdfPath) {
		try {
			char[] password = keystorePwd.toCharArray();
	        CreateSignaturePades signing = new CreateSignaturePades(keystore, password);
	        signing.setExternalSigning(false);
	
	        File outFile = null;
	        if (signedPdfPath == null || signedPdfPath.isEmpty()) {
	        	String name = pdfToSign.getName();
	        	String substring = name.substring(0, name.lastIndexOf('.'));
	        	outFile = new File(pdfToSign.getParent(), substring + "_signed.pdf");
	        } else {
	        	outFile = new File(signedPdfPath);
	        }
	        signing.signDetached(pdfToSign, outFile, null);
	        return true;  
		} catch (Exception e) {
			log.error("Error while running sign : ", e);
			return false;
		}
	}

	
}
