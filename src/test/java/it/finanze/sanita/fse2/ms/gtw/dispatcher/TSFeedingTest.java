package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.Assertions;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.PublicationCreationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthDataFormatEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthcareFacilityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PracticeSettingCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.TipoDocAltoLivEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.FileUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "kafka.topic=junit-ts")
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
public class TSFeedingTest {


	@Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    @Autowired
	private RestTemplate restTemplate;


    @Test
    @DisplayName("Happy path")
    void t1() {

        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");
        
        final String transactionID = StringUtility.generateUUID();

        PublicationCreationReqDTO requestBody = PublicationCreationReqDTO.builder()
                .transactionID(transactionID)
                .healthDataFormat(HealthDataFormatEnum.CDA)
                .mode(InjectionModeEnum.ATTACHMENT)
                .tipologiaStruttura(HealthcareFacilityEnum.OSPEDALE)
                .regoleAccesso(java.util.Arrays.asList(EventCodeEnum.P99))
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
                .forcePublish(false)
                .build();

        PublicationCreationResDTO result = callTSEndpoint(requestBody, pdfAttachment);
        assertNotNull(result);
    }


    @Test
    @DisplayName("Mining CDA error")
    void t2() { 

        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");

        PublicationCreationReqDTO errorRequestBody = PublicationCreationReqDTO.builder()
                .transactionID(StringUtility.generateUUID())
                .healthDataFormat(HealthDataFormatEnum.DICOM_SR)
                .mode(InjectionModeEnum.RESOURCE)
                .tipologiaStruttura(HealthcareFacilityEnum.PREVENZIONE)
                .regoleAccesso(java.util.Arrays.asList(EventCodeEnum.J07BX03))
                .identificativoDoc(StringUtility.generateUUID())
                .identificativoRep(StringUtility.generateUUID())
                .tipoDocumentoLivAlto(TipoDocAltoLivEnum.ESENZIONE)
                .assettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC002)
                .identificativoPaziente(StringUtility.generateUUID())
                .dataInizioPrestazione("16/06/1995")
                .dataFinePrestazione("20/06/1995")
                .conservazioneSostitutiva("string")
                .tipoAttivitaClinica(AttivitaClinicaEnum.DOCUMENTI_SISTEMA_TS)
                .identificativoSottomissione(StringUtility.generateUUID())
                .forcePublish(false)
                .build();


        HttpClientErrorException thrown = Assertions.assertThrows(HttpClientErrorException.class, () -> {
            callTSEndpoint(errorRequestBody, pdfAttachment);
        }, "HttpClientErrorException error was expected");

        Assertions.assertEquals(400, thrown.getRawStatusCode());
    }


    private PublicationCreationResDTO callTSEndpoint(PublicationCreationReqDTO requestBody, byte[] fileByte) { 
        ObjectMapper objectMapper = new ObjectMapper(); 
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  
        
        String urlValidation = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath() + "/v1/ts-feeding";
 
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
    
}
