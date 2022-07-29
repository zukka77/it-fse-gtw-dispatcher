package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

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
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IValidatorClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.FhirMappingClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTHeaderDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.PublicationCreationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.DocumentReferenceResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthDataFormatEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthcareFacilityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PracticeSettingCodeEnum;
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
	private MicroservicesURLCFG msCfg;

	@MockBean
	private FhirMappingClient client;
	
	@Test
	void testHashPublication() {

		given(msCfg.getFromGovway()).willReturn(false);
		log.info("Testing hash check in publication phase");
		final String txID = UUID.randomUUID().toString();
		final String hash = StringUtility.encodeSHA256B64("hash");
		final String expectedHash = StringUtility.encodeSHA256B64("expected_hash");

		assertThrows(BusinessException.class, () -> cdaFacadeSRV.retrieveValidationInfo(hash, null), "If the txId is null, a Business exception should be thrown.");
		assertFalse(cdaFacadeSRV.retrieveValidationInfo(hash, txID).isCdaValidated(), "If the hash is not present on Redis, the result should be false.");

		log.info("Inserting a key on Redis");
		cdaFacadeSRV.create(txID, hash);
		assertTrue(cdaFacadeSRV.retrieveValidationInfo(hash, txID).isCdaValidated(), "If the hash is present on Redis, the result should be true");

		final String unmatchinTxID = UUID.randomUUID().toString();
		cdaFacadeSRV.create(unmatchinTxID, expectedHash);

		assertFalse(cdaFacadeSRV.retrieveValidationInfo(hash, unmatchinTxID).isCdaValidated(), "If the hash present on Redis is different from expected one, the result should be false");
	}
	
	@Test
	@DisplayName("Validation + Publication")
	void testPublication() {

		given(msCfg.getFromGovway()).willReturn(false);

		mockDocumentRef();		
        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");

		ValidationInfoDTO info = new ValidationInfoDTO(RawValidationEnum.OK, new ArrayList<>());
		given(validatorClient.validate(anyString())).willReturn(info);

		Map<String,ValidationResultEnum> res = callValidation(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdfAttachment, true, false);
		Optional<String> firstKey = res.keySet().stream().findFirst();

		String transactionId = "";
		if (firstKey.isPresent()) {
			transactionId = firstKey.get();
		}
		PublicationCreationResDTO resPublication = callPublication(pdfAttachment,null, transactionId, msCfg.getFromGovway());
		assertNotNull(resPublication.getTraceID()); 
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

	@Test
	void patternTest() {
		final String devUrl = "http:localhost:9080";
		final String prodUrl = "https://server-ok.com";

		final Pattern pattern = Pattern.compile("^https://.*");
		assertTrue(pattern.matcher(prodUrl).matches());
		assertFalse(pattern.matcher(devUrl).matches());
	}

	@Test
	void govwayHeader () {
		given(msCfg.getFromGovway()).willReturn(true);

		mockDocumentRef();

		final byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");
		final ValidationInfoDTO info = new ValidationInfoDTO(RawValidationEnum.OK, new ArrayList<>());
		given(validatorClient.validate(anyString())).willReturn(info); // Mocking validation

		final Map<String,ValidationResultEnum> res = callValidation(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdfAttachment, true, true);
		final Optional<String> firstKey = res.keySet().stream().findFirst();

		String transactionId = "";
		if (firstKey.isPresent()) {
			transactionId = firstKey.get();
		}

		final PublicationCreationResDTO resPublication = callPublication(pdfAttachment, null, transactionId, msCfg.getFromGovway());
		assertNotNull(resPublication.getTraceID());
	}

	private void mockDocumentRef() {
		DocumentReferenceResDTO ref = new DocumentReferenceResDTO();
		ref.setErrorMessage("");
		ref.setJson("{\"json\" : \"json\"}");
		given(client.callCreateDocumentReference(any(DocumentReferenceDTO.class))).willReturn(ref);
	}

	PublicationCreationResDTO callPublication(final byte[] fileByte, final PublicationCreationReqDTO reqDTO, final String transactionId, final boolean isFromGovway) {
		PublicationCreationResDTO output = null;
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
			
			if (isFromGovway) {
				headers.set(Constants.Headers.JWT_GOVWAY_HEADER, generateJwtPayload(StringUtility.encodeSHA256(fileByte)));
			} else {
				headers.set(Constants.Headers.JWT_HEADER, generateJwt(StringUtility.encodeSHA256(fileByte)));
			}

			String urlPublication = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath() + "/v1/documents";

			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

			ResponseEntity<PublicationCreationResDTO> response = restTemplate.exchange(urlPublication, HttpMethod.POST, requestEntity, PublicationCreationResDTO.class);
			output = response.getBody();
		} catch (Exception ex) {
			String message = ex.getMessage();
			Integer firstIndex = message.indexOf("{");
			Integer lastIndex = message.indexOf("}");
			String subString = message.substring(firstIndex, lastIndex+1);

			ErrorResponseDTO errorClass = StringUtility.fromJSON(subString, ErrorResponseDTO.class);
			log.info("Status {}", errorClass.getStatus());
			log.error("Error : " + ex.getMessage());
		}
		return output;
	}

	PublicationCreationReqDTO buildCreationDTO(String transactionId) {
		PublicationCreationReqDTO output = PublicationCreationReqDTO.builder().
				assettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001).
				conservazioneANorma("Conservazione sostitutiva").
				dataFinePrestazione(""+new Date().getTime()).
				dataInizioPrestazione(""+new Date().getTime()).
				healthDataFormat(HealthDataFormatEnum.CDA).
				identificativoDoc("Identificativo doc").
				identificativoRep("Identificativo rep").
				identificativoSottomissione("Identificativo Sottomissione").
				mode(InjectionModeEnum.ATTACHMENT).
				attiCliniciRegoleAccesso(java.util.Arrays.asList(EventCodeEnum._94503_0.getCode())).
				tipoAttivitaClinica(AttivitaClinicaEnum.CON).
				tipoDocumentoLivAlto(TipoDocAltoLivEnum.WOR).
				tipologiaStruttura(HealthcareFacilityEnum.Ospedale).
				workflowInstanceId(transactionId).
				build();
		return output;
	}

}
