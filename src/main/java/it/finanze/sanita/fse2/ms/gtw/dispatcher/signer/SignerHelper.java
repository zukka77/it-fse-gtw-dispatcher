package it.finanze.sanita.fse2.ms.gtw.dispatcher.signer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.SignatureInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.SignatureValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.SignedContentDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.SignVerificationModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.signer.signature.CreateSignaturePades;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SignerHelper {

	/******************************************************
	*	VALIDATE PADES SIGNATURE
	*******************************************************/
	
	public static SignatureValidationDTO validate(byte[] file, SignVerificationModeEnum mode) {
		SignatureValidationDTO output = null;
		Boolean status = true;
		List<SignatureInfoDTO> signatures = new ArrayList<>();
		try {
			PDDocument documentCDA = PDDocument.load(new ByteArrayInputStream(file));
			PDDocumentCatalog docCatalog = documentCDA.getDocumentCatalog();
			PDAcroForm acroForm = docCatalog.getAcroForm();
			
			if(acroForm!=null && acroForm.getFields()!=null) {
				for (PDField field:acroForm.getFields()) {
					if (field instanceof PDSignatureField) {
						PDSignatureField pf = (PDSignatureField)field;
						
						PDSignature signature = pf.getSignature();
						byte[] signatureAsBytes = signature.getContents(file);
						byte[] signedContentAsBytes = signature.getSignedContent(file);
						
						CMSSignedData cms = new CMSSignedData(new CMSProcessableByteArray(signedContentAsBytes), signatureAsBytes);
						SignerInformation signerInfo = (SignerInformation) cms.getSignerInfos().getSigners().iterator().next();
						
						X509CertificateHolder certHolder = (X509CertificateHolder) cms.getCertificates().getMatches(signerInfo.getSID()).iterator().next();
						SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder().setProvider(new BouncyCastleProvider()).build(certHolder);
						X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certHolder);
						SignatureInfoDTO info = SignatureInfoDTO.builder().
								principal(cert.getIssuerDN()).
								notBefore(cert.getNotBefore()).
								notAfter(cert.getNotAfter()).
								contactInfo(pf.getSignature().getContactInfo()).
								fieldType(field.getFieldType()).
								fullyQualifiedName(field.getFullyQualifiedName()).
								location(pf.getSignature().getLocation()).
								name(pf.getSignature().getName()).
								reason(pf.getSignature().getReason()).
								signDate(pf.getSignature().getSignDate().getTime()).
								encrypAlgOID(signerInfo.getEncryptionAlgOID()).
								digestAlgOID(signerInfo.getDigestAlgOID()).
								valid(signerInfo.verify(verifier)).
								build();
						signatures.add(info);
						status = status && info.getValid();
						
						Boolean checkDate = true;
						if (SignVerificationModeEnum.SIGNING_DAY.equals(mode)) {
							Date d = new Date();
							checkDate = (d.compareTo(cert.getNotBefore()) >= 0 && d.compareTo(cert.getNotAfter()) <= 0);
						} else if (SignVerificationModeEnum.TODAY.equals(mode)) {
							Date d = pf.getSignature().getSignDate().getTime();
							checkDate = (d.compareTo(cert.getNotBefore()) >= 0 && d.compareTo(cert.getNotAfter()) <= 0);
						}
						
						status = status && checkDate;
						
					}
				}
			} else {
				status = false;
			}
		} catch(Exception ex) {
			log.error("Error while validate signature : " , ex);
			status = false;
		}
		output = SignatureValidationDTO.builder().status(status).signatures(signatures).build(); 
        return output;
	}

	
	/******************************************************
	*	SIGNATURE PADES
	*******************************************************/
	
	private static CreateSignaturePades getSP(byte[] keyStore, char[] keystorePwd) {
		try {
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			InputStream isKeyStore = new ByteArrayInputStream(keyStore);
	        keystore.load(isKeyStore, keystorePwd);

	        CreateSignaturePades signing = new CreateSignaturePades(keystore, keystorePwd);
	        signing.setExternalSigning(false);
	        return signing;
		} catch (Exception e) {
			log.error("Error while running get sp : " ,e);
			throw new RuntimeException(e);
		}
	}

	/******************************************************
	*	GESTIONE PADES ON THE FLY
	*******************************************************/

	private static SignedContentDTO pades(CreateSignaturePades signing, String fileName, byte[] docToSign) {
		SignedContentDTO out = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	 PDDocument document = PDDocument.load(docToSign);) {

			signing.signDetached(document, baos);
			out = new SignedContentDTO(fileName, baos.toByteArray());
		} catch (Exception e) {
			log.error("Error while running pades signature : " ,e);
			out = new SignedContentDTO(e);
		}
        return out;
	}

	public static Map<String, SignedContentDTO> pades(byte[] keyStore, char[] keystorePwd, Map<String, byte[]> docsToSign) {
		Map<String, SignedContentDTO> out = new HashMap<>();
		CreateSignaturePades signing = getSP(keyStore, keystorePwd);
		for (Entry<String, byte[]> doc:docsToSign.entrySet()) {
			SignedContentDTO dto = pades(signing, doc.getKey(), doc.getValue());
			out.put(doc.getKey(), dto);
		}
		return out;
	}

	public static SignedContentDTO pades(byte[] keyStore, char[] keystorePwd, String fileName, byte[] docToSign) {
		CreateSignaturePades signing = getSP(keyStore, keystorePwd);
		return pades(signing, fileName, docToSign);
	}


	/******************************************************
	*	GESTIONE PADES FILESYSTEM
	*******************************************************/

	private static boolean padesFS(CreateSignaturePades signing, String inFileName, String outFileName) {
		boolean out = false;
        try {
			signing.signDetached(new File(inFileName), new File(outFileName));
			out = true;
		} catch (IOException e) {
			log.error("Error while running pades signature fs : " ,e);
			out = false;
		}
        return out;
	}

	public static Map<String, Boolean> padesFS(byte[] keyStore, char[] keystorePwd, Map<String, String> docsToSign) {
		Map<String, Boolean> out = new HashMap<>();
		CreateSignaturePades signing = getSP(keyStore, keystorePwd);
		for (Entry<String, String> docPair:docsToSign.entrySet()) {
			Boolean result = padesFS(signing, docPair.getKey(), docPair.getValue());
			out.put(docPair.getKey(), result);
		}
		return out;
	}

	public static boolean padesFS(byte[] keyStore, char[] keystorePwd, String inFileName, String outFileName) {
		CreateSignaturePades signing = getSP(keyStore, keystorePwd);
		return padesFS(signing, inFileName, outFileName);
	}

	public static boolean isSigned(File file) {
		boolean hasAnySignature = false;
		try(PDDocument document = PDDocument.load(file);) {
			hasAnySignature = document.getSignatureDictionaries().size() > 0;
		} catch (Exception e) {
			log.error("Error while running is signed : " ,e);
			throw new BusinessException("Error while searching signature for file with name " + file.getName(), e);
		}

		return hasAnySignature;
	}

}
