package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.PublicationCreationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ValidationCDAResDTO;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "kafka.topic=junit-historical")
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
public class HistoricalDocTest {


	@Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    @Autowired
	private RestTemplate restTemplate;


    @Test
    @DisplayName("TS Validation")
    void tsValidation() {

        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");
        
        ValidationCDAReqDTO validationRB = createValidationRB(ActivityEnum.HISTORICAL_DOC_VALIDATION);
        ValidationCDAResDTO validationResult = callHistoricalValidationEndpoint(validationRB, pdfAttachment);
        assertNotNull(validationResult);
    }


    @Test
    @DisplayName("TS: Validation + Publication")
    void validationPublication() {

        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");
        
        ValidationCDAReqDTO validationRB = createValidationRB(ActivityEnum.HISTORICAL_DOC_PRE_PUBLISHING);
        ValidationCDAResDTO validationResult = callHistoricalValidationEndpoint(validationRB, pdfAttachment);
        assertNotNull(validationResult);


        PublicationCreationReqDTO requestBody = createPublicationRB(validationResult.getTransactionId(), false);
        PublicationCreationResDTO publicationResult = callHistoricalPublicationEndpoint(requestBody, pdfAttachment);
        assertNotNull(publicationResult);

    }

    private ValidationCDAResDTO callHistoricalValidationEndpoint(ValidationCDAReqDTO requestBody, byte[] fileByte) { 
        ObjectMapper objectMapper = new ObjectMapper(); 
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  
        
        String urlValidation = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath() + "/v1/historical-validate-creation";
 
        ByteArrayResource fileAsResource = new ByteArrayResource(fileByte){
		    @Override
		    public String getFilename(){
		        return "file";
		    }
		};
         
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.set("Authorization", "test");
        MultiValueMap<String, Object> request = new LinkedMultiValueMap<>();


        request.add("file", fileAsResource);
        request.add("requestBody", requestBody);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(request, headers);
        return restTemplate.postForObject(urlValidation, requestEntity, ValidationCDAResDTO.class);
    }

    private PublicationCreationResDTO callHistoricalPublicationEndpoint(PublicationCreationReqDTO requestBody, byte[] fileByte) { 
        ObjectMapper objectMapper = new ObjectMapper(); 
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  
        
        String urlValidation = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath() + "/v1/historical-publish-creation";
 
        ByteArrayResource fileAsResource = new ByteArrayResource(fileByte){
		    @Override
		    public String getFilename(){
		        return "file";
		    }
		};
         
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.set("Authorization", "test");
        MultiValueMap<String, Object> request = new LinkedMultiValueMap<>();


        request.add("file", fileAsResource);
        request.add("requestBody", requestBody);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(request, headers);
        return restTemplate.postForObject(urlValidation, requestEntity, PublicationCreationResDTO.class);
    }

    private ValidationCDAReqDTO createValidationRB(ActivityEnum activity) {

        return ValidationCDAReqDTO.builder()
        .activity(activity)
        .healthDataFormat(HealthDataFormatEnum.CDA)
        .mode(InjectionModeEnum.ATTACHMENT)
        .tipologiaStruttura(HealthcareFacilityEnum.OSPEDALE)
        .regoleAccesso(Arrays.asList(EventCodeEnum.P99))
        .identificativoDoc(StringUtility.generateUUID())
        .identificativoRep(StringUtility.generateUUID())
        .tipoDocumentoLivAlto(TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW)
        .assettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001)
        .identificativoPaziente(StringUtility.generateUUID())
        .dataInizioPrestazione("16/06/1995")
        .dataFinePrestazione("20/06/1995")
        .conservazioneSostitutiva("string")
        .tipoAttivitaClinica(AttivitaClinicaEnum.PERSONAL_HEALTH_RECORD_UPDATE)
        .identificativoSottomissione(StringUtility.generateUUID())
        .build();

    }

    private PublicationCreationReqDTO createPublicationRB(final String transactionID, final boolean forcePublish) {

        return PublicationCreationReqDTO.builder()
        .transactionID(transactionID)
        .healthDataFormat(HealthDataFormatEnum.CDA)
        .mode(InjectionModeEnum.ATTACHMENT)
        .tipologiaStruttura(HealthcareFacilityEnum.OSPEDALE)
        .regoleAccesso(Arrays.asList(EventCodeEnum.P99))
        .identificativoDoc(StringUtility.generateUUID())
        .identificativoRep(StringUtility.generateUUID())
        .tipoDocumentoLivAlto(TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW)
        .assettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001)
        .identificativoPaziente(StringUtility.generateUUID())
        .dataInizioPrestazione("16/06/1995")
        .dataFinePrestazione("20/06/1995")
        .conservazioneSostitutiva("string")
        .tipoAttivitaClinica(AttivitaClinicaEnum.PERSONAL_HEALTH_RECORD_UPDATE)
        .identificativoSottomissione(StringUtility.generateUUID())
        .forcePublish(forcePublish)
        .build();

    }
    
}
