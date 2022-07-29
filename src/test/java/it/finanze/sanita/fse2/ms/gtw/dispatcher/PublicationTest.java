package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import java.util.regex.Pattern;

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
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.PublicationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ValidationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.DocumentReferenceResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthDataFormatEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthcareFacilityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PracticeSettingCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RawValidationEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.TipoDocAltoLivEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.redis.impl.CdaRepo;
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

	@MockBean
	private MicroservicesURLCFG msCfg;

	@Autowired
	private CdaRepo cdaRepo;

	@Test
	void t1() {
		// non pdf file
    	byte[] wrongPdf = FileUtility.getFileFromInternalResources("Files/Test.docx");
        RestExecutionResultEnum resPublication = callPublication(wrongPdf,null, "aaaaa", false, true);
		assertNotNull(resPublication); 
	}
	
	@Test
	void testHashPublication() {

		log.info("Testing hash check in publication phase");
		final String wii = UUID.randomUUID().toString();
		final String hash = StringUtility.encodeSHA256B64("hash");
		final String unmatchingHash = hash + "A"; // Modified hash

		assertFalse(cdaFacadeSRV.retrieveValidationInfo(hash, wii).isCdaValidated(), "If the hash is not present on Redis, the result should be false.");

		log.info("Inserting a key on Redis");
		cdaFacadeSRV.create(hash, wii);
		assertTrue(cdaFacadeSRV.retrieveValidationInfo(hash, wii).isCdaValidated(), "If the hash is present on Redis, the result should be true");
		
		assertFalse(cdaFacadeSRV.retrieveValidationInfo(unmatchingHash, wii).isCdaValidated(), "If the hash present on Redis is different from expected one, the result should be false");
	}

	@Test
	@DisplayName("Validation + Publication")
	void testPublication() {
		DocumentReferenceResDTO ref = new DocumentReferenceResDTO();
		ref.setErrorMessage("");
		ref.setJson("{\"json\" : \"json\"}");
		given(client.callConvertCdaInBundle(any(FhirResourceDTO.class))).willReturn(ref);

		byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");

		ValidationInfoDTO info = new ValidationInfoDTO(RawValidationEnum.OK, new ArrayList<>());
		given(validatorClient.validate(anyString())).willReturn(info);

		Map<String,RestExecutionResultEnum> res = callValidation(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdfAttachment, true, false, true);
		Optional<String> firstKey = res.keySet().stream().findFirst();

		String transactionId = "";
		if (firstKey.isPresent()) {
			transactionId = firstKey.get();
		}

		cdaRepo.create("6XpKL8W/lQjXvnZ24wFNts5itL07id7suEe+YluhfcY=", "wfid");

		PublicationCreationReqDTO reqDTO = buildReqDTO();

		RestExecutionResultEnum resPublication = callPublication(pdfAttachment, reqDTO, transactionId, false, true);
		assertNotNull(resPublication);
		assertEquals(RestExecutionResultEnum.OK.getType(), resPublication.getType());

	}

	@Test
    @DisplayName("File tests")
    void fileTests() {

        String transactionID = UUID.randomUUID().toString();

        // non pdf file
    	byte[] wrongPdf = FileUtility.getFileFromInternalResources("Files/Test.docx");
        RestExecutionResultEnum resPublication = callPublication(wrongPdf,null, transactionID, false, false);
		assertNotNull(resPublication);
        assertEquals(RestExecutionResultEnum.DOCUMENT_TYPE_ERROR.getType(), resPublication.getType());

        // attachment pdf - wong mode
    	byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_ATTACHMENT.pdf");
		resPublication = callPublication(pdfAttachment,buildCreationDTO(transactionID, InjectionModeEnum.RESOURCE), transactionID, false, true);
		assertNotNull(resPublication); 
        assertEquals(RestExecutionResultEnum.MINING_CDA_ERROR.getType(), resPublication.getType());

        // attachment resource - wong mode
    	byte[] pdfResource = FileUtility.getFileFromInternalResources("Files/resource/CDA_RESOURCE.pdf");
		resPublication = callPublication(pdfResource,buildCreationDTO(transactionID, InjectionModeEnum.ATTACHMENT), transactionID, false, true);
		assertNotNull(resPublication); 
        assertEquals(RestExecutionResultEnum.MINING_CDA_ERROR.getType(), resPublication.getType());
        

    }


	@Test
	void jwtValidation () {
		byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/LDO_OK.pdf");
		String encoded = StringUtility.encodeSHA256(pdfAttachment);
		String token = generateJwt(pdfAttachment, true);
		
		log.info("Token: {}", token);
		
		String noBearer = token.substring(7);
		String[] split = noBearer.split("\\.");

		String payload = new String(Base64.getDecoder().decode(split[1]));
		
		JWTTokenDTO jwtToken = new JWTTokenDTO(JWTPayloadDTO.extractPayload(payload));

		assertNotNull(jwtToken);
		assertNotNull(jwtToken.getPayload());
		assertEquals(encoded, jwtToken.getPayload().getAttachment_hash());
	}

	public RestExecutionResultEnum callPublication(byte[] fileByte, PublicationCreationReqDTO reqDTO, String transactionId, final boolean fromGoveway, boolean isValidFile) {
		RestExecutionResultEnum output = null;
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
			
			if (fromGoveway) {
				headers.set(Constants.Headers.JWT_GOVWAY_HEADER, generateJwtGovwayPayload(fileByte));
			} else {
				headers.set(Constants.Headers.JWT_HEADER, generateJwt(fileByte, isValidFile));
			}

			String urlPublication = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath() + "/v1/documents";

			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

			restTemplate.exchange(urlPublication, HttpMethod.POST, requestEntity, PublicationResDTO.class);
			return RestExecutionResultEnum.OK;
		} catch (Exception ex) {
			String message = ex.getMessage();
			Integer firstIndex = message.indexOf("{");
			Integer lastIndex = message.indexOf("}");
			String subString = message.substring(firstIndex, lastIndex + 1);

			ErrorResponseDTO errorClass = StringUtility.fromJSON(subString, ErrorResponseDTO.class);
			output = RestExecutionResultEnum.get(errorClass.getType());
			log.info("Status {}", errorClass.getStatus());
			log.error("Error : " + ex.getMessage());
		}
		return output;
	}

	private PublicationCreationReqDTO buildCreationDTO(String workflowInstanceId) {
		PublicationCreationReqDTO output = PublicationCreationReqDTO.builder().
				assettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001).
				conservazioneANorma("Conservazione sostitutiva").
				dataFinePrestazione(""+new Date().getTime()).
				dataInizioPrestazione(""+new Date().getTime()).
				forcePublish(false).
				healthDataFormat(HealthDataFormatEnum.CDA).
				identificativoDoc("Identificativo doc").
				identificativoRep("Identificativo rep").
				identificativoSottomissione("Identificativo Sottomissione").
				mode(InjectionModeEnum.ATTACHMENT).
				attiCliniciRegoleAccesso(java.util.Arrays.asList(EventCodeEnum._94503_0)).
				tipoAttivitaClinica(AttivitaClinicaEnum.CON).
				tipoDocumentoLivAlto(TipoDocAltoLivEnum.WOR).
				tipologiaStruttura(HealthcareFacilityEnum.Ospedale).
				workflowInstanceId(workflowInstanceId).
				build();
		return output;
	}

	private PublicationCreationReqDTO buildCreationDTO(String workflowInstanceId, InjectionModeEnum mode) {
		PublicationCreationReqDTO output = PublicationCreationReqDTO.builder().
				assettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001).
				conservazioneANorma("Conservazione sostitutiva").
				dataFinePrestazione(""+new Date().getTime()).
				dataInizioPrestazione(""+new Date().getTime()).
				forcePublish(false).
				healthDataFormat(HealthDataFormatEnum.CDA).
				identificativoDoc("Identificativo doc").
				identificativoRep("Identificativo rep").
				identificativoSottomissione("Identificativo Sottomissione").
				mode(mode).
				attiCliniciRegoleAccesso(java.util.Arrays.asList(EventCodeEnum._94503_0)).
				tipoAttivitaClinica(AttivitaClinicaEnum.CON).
				tipoDocumentoLivAlto(TipoDocAltoLivEnum.WOR).
				tipologiaStruttura(HealthcareFacilityEnum.Ospedale).
				workflowInstanceId(workflowInstanceId).
				build();
		return output;
	}

	@Test
	void publicationErrorTest() {

		given(client.callConvertCdaInBundle(any(FhirResourceDTO.class))).willReturn(new DocumentReferenceResDTO("", "{\"json\" : \"json\"}"));
        final byte[] file = FileUtility.getFileFromInternalResources("Files" + File.separator + "attachment" + File.separator + "CDA_OK_SIGNED.pdf");
		final String jwtToken = generateJwt(file, true);
		
		ValidationCDAReqDTO validationRB = validateDataPreparation();
		
		// Mocking validator
		ValidationInfoDTO info = new ValidationInfoDTO(RawValidationEnum.OK, new ArrayList<>());
		given(validatorClient.validate(anyString())).willReturn(info);

		ResponseEntity<ValidationResDTO> response = callPlainValidation(jwtToken, file, validationRB);
		assertEquals(HttpStatus.SC_CREATED, response.getStatusCode().value());
		final String workflowInstanceId = response.getBody().getWorkflowInstanceId();

		PublicationCreationReqDTO publicationRB = new PublicationCreationReqDTO();
		Exception thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setWorkflowInstanceId(workflowInstanceId);
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setTipologiaStruttura(HealthcareFacilityEnum.Ospedale);
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setIdentificativoDoc("identificativoDoc");
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setIdentificativoRep("identificativoRep");
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setTipoDocumentoLivAlto(TipoDocAltoLivEnum.REF);
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setAssettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC055);
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setAssettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC055);
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
	
		publicationRB.setTipoAttivitaClinica(AttivitaClinicaEnum.CON);
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		assertTrue(thrownException.getMessage().contains(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()));
		
		publicationRB.setIdentificativoSottomissione("identificativoSottomissione");
		publicationRB.setIdentificativoDoc(TestConstants.documentId);
		assertDoesNotThrow(() -> callPlainPublication(jwtToken, file, publicationRB));
		
		thrownException = assertThrows(HttpClientErrorException.Forbidden.class, () -> callPlainPublication(null, file, publicationRB));
		
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, new byte[0], publicationRB));
		assertTrue(thrownException.getMessage().contains(RestExecutionResultEnum.EMPTY_FILE_ERROR.getType()));
	
		publicationRB.setWorkflowInstanceId("NON EXISTING");
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(jwtToken, file, publicationRB));
		log.info(ExceptionUtils.getStackTrace(thrownException));
	}
	
	@Test
	void publicationWarningTest() {

		given(client.callConvertCdaInBundle(any(FhirResourceDTO.class))).willReturn(new DocumentReferenceResDTO("", "{\"json\" : \"json\"}"));
        final byte[] file = FileUtility.getFileFromInternalResources("Files" + File.separator + "attachment" + File.separator + "CDA_OK_SIGNED.pdf");
		final String jwtToken = generateJwt(file, true);
		
		ValidationCDAReqDTO validationRB = validateDataPreparation();
		
		// Mocking validator
		ValidationInfoDTO info = new ValidationInfoDTO(RawValidationEnum.OK, new ArrayList<>());
		given(validatorClient.validate(anyString())).willReturn(info);

		ResponseEntity<ValidationResDTO> response = callPlainValidation(jwtToken, file, validationRB);
		assertEquals(HttpStatus.SC_CREATED, response.getStatusCode().value());
		final String workflowInstanceId = response.getBody().getWorkflowInstanceId();

		PublicationCreationReqDTO publicationRB = new PublicationCreationReqDTO();
	
		publicationRB.setWorkflowInstanceId(workflowInstanceId);
		publicationRB.setTipologiaStruttura(HealthcareFacilityEnum.Ospedale);	
		publicationRB.setIdentificativoDoc("identificativoDoc");
		publicationRB.setIdentificativoRep("identificativoRep");
		publicationRB.setMode(null);
		publicationRB.setTipoDocumentoLivAlto(TipoDocAltoLivEnum.REF);
		publicationRB.setAssettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC055);		
		publicationRB.setTipoAttivitaClinica(AttivitaClinicaEnum.CON);
		publicationRB.setIdentificativoSottomissione("identificativoSottomissione");
		publicationRB.setIdentificativoDoc(TestConstants.documentId);
		
		final ResponseEntity<PublicationResDTO> publicationResponse = callPlainPublication(jwtToken, file, publicationRB);

		assertEquals(Constants.Misc.WARN_EXTRACTION_SELECTION, publicationResponse.getBody().getWarning());
		assertNotNull(publicationResponse.getBody().getWarning());
		assertDoesNotThrow(()->publicationResponse);
	}

	@Test
	void publicationForcedTest() {

		given(client.callConvertCdaInBundle(any(FhirResourceDTO.class))).willReturn(new DocumentReferenceResDTO("", "{\"json\" : \"json\"}"));
        final byte[] file = FileUtility.getFileFromInternalResources("Files" + File.separator + "attachment" + File.separator + "CDA_OK_SIGNED.pdf");
		final String jwtToken = generateJwt(file, true);
		
		final ValidationCDAReqDTO validationRB = validateDataPreparation();
		
		
		// Mocking validator
		final ValidationInfoDTO info = new ValidationInfoDTO(RawValidationEnum.OK, new ArrayList<>());
		given(validatorClient.validate(anyString())).willReturn(info);

		final ResponseEntity<ValidationResDTO> validationResponse = callPlainValidation(jwtToken, file, validationRB);
		assumeFalse(validationResponse == null);

		final PublicationCreationReqDTO publicationRB = publicationDataPreparation();

		final ResponseEntity<PublicationResDTO> publicationResponse = callPlainPublication(jwtToken, file, publicationRB);
		assertNotNull(publicationResponse);
		assertEquals(HttpStatus.SC_CREATED, publicationResponse.getStatusCode().value());
		assertNull(publicationResponse.getBody().getWarning());
		
		// Mocking Validator - null InjectionModeEnum param
		final ValidationCDAReqDTO validationRBnullMode = validateDataPreparation();
		validationRBnullMode.setMode(null);
		
		final ResponseEntity<ValidationResDTO> validationResponseNullMode = callPlainValidation(jwtToken, file, validationRBnullMode);
		assumeFalse(validationResponseNullMode == null);
		
		final PublicationCreationReqDTO publicationRBnullMode = publicationDataPreparation();
		publicationRBnullMode.setMode(null);
		
		final ResponseEntity<PublicationResDTO> publicationResponseNullMode = callPlainPublication(jwtToken, file, publicationRBnullMode);
		assertNotNull(publicationResponseNullMode.getBody().getWarning());
		assertEquals(Constants.Misc.WARN_EXTRACTION_SELECTION, publicationResponseNullMode.getBody().getWarning());
		
	}

	@Test
	void govwayHeader () {

		given(msCfg.getFromGovway()).willReturn(true);
		mockDocumentRef();

		final byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");
		final ValidationInfoDTO info = new ValidationInfoDTO(RawValidationEnum.OK, new ArrayList<>());
		given(validatorClient.validate(anyString())).willReturn(info); // Mocking validation

		final Map<String, RestExecutionResultEnum> res = callValidation(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdfAttachment, true, true, true);
		final Optional<String> firstKey = res.keySet().stream().findFirst();

		String transactionId = "";
		if (firstKey.isPresent()) {
			transactionId = firstKey.get();
		}

		cdaRepo.create("6XpKL8W/lQjXvnZ24wFNts5itL07id7suEe+YluhfcY=", "wfid");
		PublicationCreationReqDTO reqDTO = buildReqDTO();

		final RestExecutionResultEnum resPublication = callPublication(pdfAttachment, reqDTO, transactionId, msCfg.getFromGovway(), true);
		assertEquals(RestExecutionResultEnum.OK, resPublication);
	}

	private void mockDocumentRef() {
		DocumentReferenceResDTO ref = new DocumentReferenceResDTO();
		ref.setErrorMessage("");
		ref.setJson("{\"json\" : \"json\"}");
		given(client.callConvertCdaInBundle(any(FhirResourceDTO.class))).willReturn(ref);
	}

	@Test
	@DisplayName("error fhir creation")
	void errorFhirResourceCreationTest() {
		final byte[] file = FileUtility.getFileFromInternalResources("Files" + File.separator + "attachment" + File.separator + "CDA_OK_SIGNED.pdf");
		final String jwtToken = generateJwt(file, true);

		final ValidationCDAReqDTO validationRB = validateDataPreparation();

		// Mocking validator
		final ValidationInfoDTO info = new ValidationInfoDTO(RawValidationEnum.OK, new ArrayList<>());
		given(validatorClient.validate(anyString())).willReturn(info);

		final ResponseEntity<ValidationResDTO> validationResponse = callPlainValidation(jwtToken, file, validationRB);
		assumeFalse(validationResponse == null);

		final PublicationCreationReqDTO publicationRB = publicationDataPreparation();

		ResourceDTO fhirResourcesDTO = new ResourceDTO();
		fhirResourcesDTO.setErrorMessage("Errore generico");

		given(client.callConvertCdaInBundle(any(FhirResourceDTO.class)))
				.willReturn(new DocumentReferenceResDTO("Errore generico", null));

		assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainPublication(
				jwtToken,
				file,
				publicationRB
		));
	}

	@Test
	void patternTest() {
		final String devUrl = "http:localhost:9080";
		final String prodUrl = "https://server-ok.com";
		final Pattern pattern = Pattern.compile("^https://.*");
		assertTrue(pattern.matcher(prodUrl).matches());
		assertFalse(pattern.matcher(devUrl).matches());
	}
}
