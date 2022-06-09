package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.FhirMappingClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.HistoricalPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.HistoricalValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.HistoricalPublicationCreationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.HistoricalValidationCDAResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.DocumentReferenceResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthDataFormatEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthcareFacilityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PracticeSettingCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.TipoDocAltoLivEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.FileUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "kafka.topic=junit-historical")
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
@Slf4j
public class HistoricalDocTest extends AbstractTest {


	@Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    @Autowired
	private RestTemplate restTemplate;

    @MockBean
	private FhirMappingClient fhirMappingClient;


    @Test
    @DisplayName("Historical Doc Validation")
    void validation() {

        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");
        
        HistoricalValidationCDAReqDTO validationRB = createValidationRB(ActivityEnum.HISTORICAL_DOC_VALIDATION);
        HistoricalValidationCDAResDTO validationResult = callHistoricalValidationEndpoint(validationRB, pdfAttachment);
        assertNotNull(validationResult);
    }


    @Test
    @DisplayName("Historical Doc Validation + Publication")
    void validationPublication() {

        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");
        
        HistoricalValidationCDAReqDTO validationRB = createValidationRB(ActivityEnum.HISTORICAL_DOC_PRE_PUBLISHING);
        HistoricalValidationCDAResDTO validationResult = callHistoricalValidationEndpoint(validationRB, pdfAttachment);
        assertNotNull(validationResult.getTraceID());

        // mock fhir mapping call
        DocumentReferenceResDTO ref = new DocumentReferenceResDTO();
		ref.setErrorMessage("");
		ref.setJson("{\"json\" : \"json\"}");
		given(fhirMappingClient.callCreateDocumentReference(any(DocumentReferenceDTO.class))).willReturn(ref);
        
        HistoricalPublicationCreationReqDTO requestBody = createPublicationRB(validationResult.getWorkflowInstanceId(), false);
        HistoricalPublicationCreationResDTO publicationResult = callHistoricalPublication(pdfAttachment, requestBody);
        assertNotNull(publicationResult.getTraceID());

    }

    private HistoricalValidationCDAResDTO callHistoricalValidationEndpoint(HistoricalValidationCDAReqDTO requestBody, byte[] fileByte) { 
        ObjectMapper objectMapper = new ObjectMapper(); 
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  
        
        String urlValidation = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath() + "/v1.0.0/historical-validate-creation";
 
        ByteArrayResource fileAsResource = new ByteArrayResource(fileByte){
		    @Override
		    public String getFilename(){
		        return "file";
		    }
		};
         
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		log.info("Simulating a valid json payload");

        headers.set(Constants.Headers.JWT_HEADER, generateJwt(StringUtility.encodeSHA256(fileByte)));

        MultiValueMap<String, Object> request = new LinkedMultiValueMap<>();


        request.add("file", fileAsResource);
        request.add("requestBody", requestBody);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(request, headers);
        return restTemplate.postForObject(urlValidation, requestEntity, HistoricalValidationCDAResDTO.class);
    }

    public HistoricalPublicationCreationResDTO callHistoricalPublication(byte[] fileByte, HistoricalPublicationCreationReqDTO reqDTO) {

		HistoricalPublicationCreationResDTO output = null;
		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

		try {
			ByteArrayResource fileAsResource = new ByteArrayResource(fileByte){
				@Override
				public String getFilename(){
					return "file";
				}
			};

			map.add("file",fileAsResource);
            map.add("requestBody", reqDTO);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			headers.set(Constants.Headers.JWT_HEADER, generateJwt(StringUtility.encodeSHA256(fileByte)));

			String urlPublication = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath() + "/v1.0.0/historical-publish-creation";

			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

			ResponseEntity<HistoricalPublicationCreationResDTO> response = restTemplate.exchange(urlPublication, HttpMethod.POST, requestEntity, HistoricalPublicationCreationResDTO.class);
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

    private HistoricalValidationCDAReqDTO createValidationRB(ActivityEnum activity) {

        return HistoricalValidationCDAReqDTO.builder()
        .activity(activity)
        .healthDataFormat(HealthDataFormatEnum.CDA)
        .mode(InjectionModeEnum.ATTACHMENT)
        .tipologiaStruttura(HealthcareFacilityEnum.OSPEDALE)
        .regoleAccesso(Arrays.asList(EventCodeEnum.P99))
        .identificativoDoc(StringUtility.generateUUID())
        .identificativoRep(StringUtility.generateUUID())
        .tipoDocumentoLivAlto(TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW)
        .assettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001)
        .identificativoPaziente(randomFiscalCode())
        .dataInizioPrestazione("1652284461782")
        .dataFinePrestazione("1652284497269")
        .conservazioneSostitutiva("string")
        .tipoAttivitaClinica(AttivitaClinicaEnum.PERSONAL_HEALTH_RECORD_UPDATE)
        .identificativoSottomissione(StringUtility.generateUUID())
        .build();

    }

    private HistoricalPublicationCreationReqDTO createPublicationRB(final String workflowInstanceId, final boolean forcePublish) {

        return HistoricalPublicationCreationReqDTO.builder()
        .workflowInstanceId(workflowInstanceId)
        .healthDataFormat(HealthDataFormatEnum.CDA)
        .mode(InjectionModeEnum.ATTACHMENT)
        .tipologiaStruttura(HealthcareFacilityEnum.OSPEDALE)
        .regoleAccesso(Arrays.asList(EventCodeEnum.P99))
        .identificativoDoc(StringUtility.generateUUID())
        .identificativoRep(StringUtility.generateUUID())
        .tipoDocumentoLivAlto(TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW)
        .assettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001)
        .identificativoPaziente(randomFiscalCode())
        .dataInizioPrestazione("1652284461782")
        .dataFinePrestazione("1652284497269")
        .conservazioneSostitutiva("string")
        .tipoAttivitaClinica(AttivitaClinicaEnum.PERSONAL_HEALTH_RECORD_UPDATE)
        .identificativoSottomissione(StringUtility.generateUUID())
        .forcePublish(forcePublish)
        .build();

    }
    
}
