package it.finanze.sanita.fse2.ms.gtw.dispatcher;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IValidatorClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.FhirMappingClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTHeaderDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.PublicationCreationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ValidationCDAResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.DocumentReferenceResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthDataFormatEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthcareFacilityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PracticeSettingCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RawValidationEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.TipoDocAltoLivEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ValidationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.facade.ICdaFacadeSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.FileUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
class PublicationTest extends AbstractTest {

	@Autowired
	private ICdaFacadeSRV cdaFacadeSRV;

	@Autowired
	private ServletWebServerApplicationContext webServerAppCtxt;

	@Autowired
	private RestTemplate restTemplate;

	@MockBean
	private IValidatorClient validatorClient;

	@MockBean
	private FhirMappingClient client;

	@Test
	void t1() {
		// non pdf file
    	byte[] wrongPdf = FileUtility.getFileFromInternalResources("Files/Test.docx");
        PublicationResultEnum resPublication = callPublication(wrongPdf,null, "aaaaa");
		assertNotNull(resPublication); 
	}
	
	@Test
	void testHashPublication() {

		log.info("Testing hash check in publication phase");
		final String txID = UUID.randomUUID().toString();
		final String hash = StringUtility.encodeSHA256B64("hash");
		final String expectedHash = StringUtility.encodeSHA256B64("expected_hash");

		assertThrows(BusinessException.class, () -> cdaFacadeSRV.validateHash(hash, null), "If the txId is null, a Business exception should be thrown.");
		assertFalse(cdaFacadeSRV.validateHash(hash, txID), "If the hash is not present on Redis, the result should be false.");

		log.info("Inserting a key on Redis");
		cdaFacadeSRV.create(txID, hash);
		assertTrue(cdaFacadeSRV.validateHash(hash, txID), "If the hash is present on Redis, the result should be true");

		final String unmatchinTxID = UUID.randomUUID().toString();
		cdaFacadeSRV.create(unmatchinTxID, expectedHash);

		assertFalse(cdaFacadeSRV.validateHash(hash, unmatchinTxID), "If the hash present on Redis is different from expected one, the result should be false");
	}
	
	@Test
	@DisplayName("Validation + Publication")
	void testPublication() {
		DocumentReferenceResDTO ref = new DocumentReferenceResDTO();
		ref.setErrorMessage("");
		ref.setJson("{\"json\" : \"json\"}");
		given(client.callCreateDocumentReference(any(DocumentReferenceDTO.class))).willReturn(ref);
		
        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");

		ValidationInfoDTO info = new ValidationInfoDTO(RawValidationEnum.OK, new ArrayList<>());
		given(validatorClient.validate(anyString())).willReturn(info);

		Map<String,ValidationResultEnum> res = callValidation(ActivityEnum.PRE_PUBLISHING, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdfAttachment, true);
		Optional<String> firstKey = res.keySet().stream().findFirst();

		String transactionId = "";
		if (firstKey.isPresent()) {
			transactionId = firstKey.get();
		}
		PublicationResultEnum resPublication = callPublication(pdfAttachment,null, transactionId);
		assertNotNull(resPublication); 
		assertEquals(PublicationResultEnum.OK.getType(), resPublication.getType());
	}

	@Test
    @DisplayName("File tests")
    void fileTests() {

        String transactionID = UUID.randomUUID().toString();

        // non pdf file
    	byte[] wrongPdf = FileUtility.getFileFromInternalResources("Files/Test.docx");
        PublicationResultEnum resPublication = callPublication(wrongPdf,null, transactionID);
		assertNotNull(resPublication); 
        assertEquals(PublicationResultEnum.DOCUMENT_TYPE_ERROR.getType(), resPublication.getType());

        // attachment pdf - wong mode
    	byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_ATTACHMENT.pdf");
		resPublication = callPublication(pdfAttachment,buildCreationDTO(transactionID, InjectionModeEnum.RESOURCE), transactionID);
		assertNotNull(resPublication); 
        assertEquals(PublicationResultEnum.MINING_CDA_ERROR.getType(), resPublication.getType());

        // attachment resource - wong mode
    	byte[] pdfResource = FileUtility.getFileFromInternalResources("Files/resource/CDA_RESOURCE.pdf");
		resPublication = callPublication(pdfResource,buildCreationDTO(transactionID, InjectionModeEnum.ATTACHMENT), transactionID);
		assertNotNull(resPublication); 
        assertEquals(PublicationResultEnum.MINING_CDA_ERROR.getType(), resPublication.getType());

    }

	@Test
	void jwtValidation () {
		byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");
		String encoded = StringUtility.encodeSHA256(pdfAttachment);
		String token = generateJwt(StringUtility.encodeSHA256(pdfAttachment));
		
		log.info("Token: {}", token);
		
		String noBearer = token.substring(7);
		String[] split = noBearer.split("\\.");

		String header = new String(Base64.getDecoder().decode(split[0]));
		String payload = new String(Base64.getDecoder().decode(split[1]));
		
		JWTTokenDTO jwtToken = new JWTTokenDTO(JWTHeaderDTO.extractHeader(header), JWTPayloadDTO.extractPayload(payload));

		assertNotNull(jwtToken);
		assertNotNull(jwtToken.getHeader());
		assertNotNull(jwtToken.getPayload());
		assertEquals(encoded, jwtToken.getPayload().getAttachment_hash());
	}

	public PublicationResultEnum callPublication(byte[] fileByte,PublicationCreationReqDTO reqDTO, String transactionId) {
		PublicationResultEnum output = null;
		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

		try {
			ByteArrayResource fileAsResource = new ByteArrayResource(fileByte){
				@Override
				public String getFilename(){
					return "file";
				}
			};

			map.add("file",fileAsResource);

			if(reqDTO==null) {
				map.add("requestBody", buildCreationDTO(transactionId));
			} else {
				map.add("requestBody", reqDTO);
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			log.info("Simulating a valid json payload");
			
			headers.set(Constants.Headers.JWT_HEADER, generateJwt(StringUtility.encodeSHA256(fileByte)));

			String urlPublication = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath() + "/v1.0.0/publish-creation";

			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

			ResponseEntity<PublicationCreationResDTO> response = restTemplate.exchange(urlPublication, HttpMethod.POST, requestEntity, PublicationCreationResDTO.class);
			return PublicationResultEnum.OK;
		} catch (Exception ex) {
			String message = ex.getMessage();
			Integer firstIndex = message.indexOf("{");
			Integer lastIndex = message.indexOf("}");
			String subString = message.substring(firstIndex, lastIndex+1);

			ErrorResponseDTO errorClass = StringUtility.fromJSON(subString, ErrorResponseDTO.class);
			output = PublicationResultEnum.get(errorClass.getType());
			log.info("Status {}", errorClass.getStatus());
			log.error("Error : " + ex.getMessage());
		}
		return output;
	}

	private PublicationCreationReqDTO buildCreationDTO(String workflowInstanceId) {
		PublicationCreationReqDTO output = PublicationCreationReqDTO.builder().
				assettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001).
				conservazioneSostitutiva("Conservazione sostitutiva").
				dataFinePrestazione(""+new Date().getTime()).
				dataInizioPrestazione(""+new Date().getTime()).
				forcePublish(false).
				healthDataFormat(HealthDataFormatEnum.CDA).
				identificativoDoc("Identificativo doc").
				identificativoPaziente(randomFiscalCode()).
				identificativoRep("Identificativo rep").
				identificativoSottomissione("Identificativo Sottomissione").
				mode(InjectionModeEnum.ATTACHMENT).
				regoleAccesso(java.util.Arrays.asList(EventCodeEnum._94503_0)).
				tipoAttivitaClinica(AttivitaClinicaEnum.CONSULTO).
				tipoDocumentoLivAlto(TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW).
				tipologiaStruttura(HealthcareFacilityEnum.OSPEDALE).
				workflowInstanceId(workflowInstanceId).
				build();
		return output;
	}

	private PublicationCreationReqDTO buildCreationDTO(String workflowInstanceId, InjectionModeEnum mode) {
		PublicationCreationReqDTO output = PublicationCreationReqDTO.builder().
				assettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001).
				conservazioneSostitutiva("Conservazione sostitutiva").
				dataFinePrestazione(""+new Date().getTime()).
				dataInizioPrestazione(""+new Date().getTime()).
				forcePublish(false).
				healthDataFormat(HealthDataFormatEnum.CDA).
				identificativoDoc("Identificativo doc").
				identificativoPaziente(randomFiscalCode()).
				identificativoRep("Identificativo rep").
				identificativoSottomissione("Identificativo Sottomissione").
				mode(mode).
				regoleAccesso(java.util.Arrays.asList(EventCodeEnum._94503_0)).
				tipoAttivitaClinica(AttivitaClinicaEnum.CONSULTO).
				tipoDocumentoLivAlto(TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW).
				tipologiaStruttura(HealthcareFacilityEnum.OSPEDALE).
				workflowInstanceId(workflowInstanceId).
				build();
		return output;
	}

	@Test
	void publicationErrorTest() {

		given(client.callCreateDocumentReference(any(DocumentReferenceDTO.class))).willReturn(new DocumentReferenceResDTO("", "{\"json\" : \"json\"}"));
        final byte[] file = FileUtility.getFileFromInternalResources("Files" + File.separator + "attachment" + File.separator + "CDA_OK_SIGNED.pdf");
		final String jwtToken = generateJwt(StringUtility.encodeSHA256(file));
		
		ValidationCDAReqDTO validationRB = new ValidationCDAReqDTO();
		validationRB.setActivity(ActivityEnum.PRE_PUBLISHING);
		validationRB.setHealthDataFormat(HealthDataFormatEnum.CDA);
		validationRB.setTipoDocumentoLivAlto(TipoDocAltoLivEnum.PRESCRIZIONE);
		validationRB.setTipoAttivitaClinica(AttivitaClinicaEnum.CONSULTO);
		validationRB.setMode(InjectionModeEnum.ATTACHMENT);
		validationRB.setIdentificativoSottomissione("identificativoSottomissione");
		validationRB.setAssettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001);
		validationRB.setIdentificativoPaziente(randomFiscalCode());
		
		// Mocking validator
		ValidationInfoDTO info = new ValidationInfoDTO(RawValidationEnum.OK, new ArrayList<>());
		given(validatorClient.validate(anyString())).willReturn(info);

		ResponseEntity<ValidationCDAResDTO> response = callPlainValidation(jwtToken, file, validationRB);
		assertEquals(HttpStatus.SC_CREATED, response.getStatusCode().value());
		final String workflowInstanceId = response.getBody().getWorkflowInstanceId();

		PublicationCreationReqDTO publicationRB = new PublicationCreationReqDTO();
		Exception thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setWorkflowInstanceId(workflowInstanceId);
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setTipologiaStruttura(HealthcareFacilityEnum.OSPEDALE);
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setIdentificativoDoc("identificativoDoc");
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setIdentificativoRep("identificativoRep");
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setTipoDocumentoLivAlto(TipoDocAltoLivEnum.REFERTO);
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setAssettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC055);
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setAssettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC055);
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setIdentificativoPaziente("INVALID FISCAL CODE");
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setIdentificativoPaziente(randomFiscalCode());
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setTipoAttivitaClinica(AttivitaClinicaEnum.CONSULTO);
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
		
		publicationRB.setIdentificativoSottomissione("identificativoSottomissione");
		assertDoesNotThrow(() -> callPlainPublication(jwtToken, file, publicationRB));
		
		thrownException = assertThrows(HttpClientErrorException.Unauthorized.class, () -> callPlainPublication(null, file, publicationRB));
		
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, new byte[0], publicationRB));
		assertTrue(thrownException.getMessage().contains(ValidationResultEnum.EMPTY_FILE_ERROR.getType()));
	
		publicationRB.setWorkflowInstanceId("NON EXISTING");
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		log.info(ExceptionUtils.getStackTrace(thrownException));
	}

	@Test
	void publicationForcedTest() {

		given(client.callCreateDocumentReference(any(DocumentReferenceDTO.class))).willReturn(new DocumentReferenceResDTO("", "{\"json\" : \"json\"}"));
        final byte[] file = FileUtility.getFileFromInternalResources("Files" + File.separator + "attachment" + File.separator + "CDA_OK_SIGNED.pdf");
		final String jwtToken = generateJwt(StringUtility.encodeSHA256(file));
		
		final ValidationCDAReqDTO validationRB = new ValidationCDAReqDTO();
		validationRB.setActivity(ActivityEnum.PRE_PUBLISHING);
		validationRB.setHealthDataFormat(HealthDataFormatEnum.CDA);
		validationRB.setTipoDocumentoLivAlto(TipoDocAltoLivEnum.PRESCRIZIONE);
		validationRB.setTipoAttivitaClinica(AttivitaClinicaEnum.CONSULTO);
		validationRB.setMode(InjectionModeEnum.ATTACHMENT);
		validationRB.setIdentificativoSottomissione("identificativoSottomissione");
		validationRB.setAssettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001);
		validationRB.setIdentificativoPaziente(randomFiscalCode());
		
		// Mocking validator
		final ValidationInfoDTO info = new ValidationInfoDTO(RawValidationEnum.OK, new ArrayList<>());
		given(validatorClient.validate(anyString())).willReturn(info);

		final ResponseEntity<ValidationCDAResDTO> validationResponse = callPlainValidation(jwtToken, file, validationRB);
		assumeFalse(validationResponse == null);

		final PublicationCreationReqDTO publicationRB = new PublicationCreationReqDTO();
		publicationRB.setWorkflowInstanceId("TXID");
		publicationRB.setTipologiaStruttura(HealthcareFacilityEnum.OSPEDALE);
		publicationRB.setIdentificativoDoc("identificativoDoc");
		publicationRB.setIdentificativoRep("identificativoRep");
		publicationRB.setTipoDocumentoLivAlto(TipoDocAltoLivEnum.REFERTO);
		publicationRB.setAssettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC055);
		publicationRB.setAssettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC055);
		publicationRB.setIdentificativoPaziente(randomFiscalCode());
		publicationRB.setTipoAttivitaClinica(AttivitaClinicaEnum.CONSULTO);
		publicationRB.setIdentificativoSottomissione("identificativoSottomissione");
		publicationRB.setForcePublish(true);

		final ResponseEntity<PublicationCreationResDTO> publicationResponse = callPlainPublication(jwtToken, file, publicationRB);
		assertNotNull(publicationResponse);
		assertEquals(HttpStatus.SC_OK, publicationResponse.getStatusCode().value());
	}
}