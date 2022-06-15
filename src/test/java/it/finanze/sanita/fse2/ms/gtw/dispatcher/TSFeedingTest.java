package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.Assertions;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.FhirMappingClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.TSPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TSPublicationCreationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.DocumentReferenceResDTO;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "kafka.topic=junit-ts")
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
@Slf4j
public class TSFeedingTest extends AbstractTest {


	@Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    @Autowired
	private RestTemplate restTemplate;

    @MockBean
	private FhirMappingClient fhirMappingClient;


    @Test
    @DisplayName("Happy path")
    void t1() {

        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");
        
        TSPublicationCreationReqDTO requestBody = TSPublicationCreationReqDTO.builder()
                .healthDataFormat(HealthDataFormatEnum.CDA)
                .mode(InjectionModeEnum.ATTACHMENT)
                .tipologiaStruttura(HealthcareFacilityEnum.OSPEDALE)
                .regoleAccesso(java.util.Arrays.asList(EventCodeEnum.P99))
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
                .forcePublish(false)
                .build();


        // mock fhir mapping call
        DocumentReferenceResDTO ref = new DocumentReferenceResDTO();
		ref.setErrorMessage("");
		ref.setJson("{\"json\" : \"json\"}");
		given(fhirMappingClient.callCreateDocumentReference(any(DocumentReferenceDTO.class))).willReturn(ref);

        TSPublicationCreationResDTO result = callTSEndpoint(requestBody, pdfAttachment);
        assertNotNull(result.getTraceID());
    }

    @Test
    @DisplayName("Force publish - skip validation")
    void t2() {

        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");
        
        TSPublicationCreationReqDTO requestBody = TSPublicationCreationReqDTO.builder()
                .healthDataFormat(HealthDataFormatEnum.CDA)
                .mode(InjectionModeEnum.ATTACHMENT)
                .tipologiaStruttura(HealthcareFacilityEnum.OSPEDALE)
                .regoleAccesso(java.util.Arrays.asList(EventCodeEnum.P99))
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
                .forcePublish(true)
                .build();

        // mock fhir mapping call
        DocumentReferenceResDTO ref = new DocumentReferenceResDTO();
		ref.setErrorMessage("");
		ref.setJson("{\"json\" : \"json\"}");
		given(fhirMappingClient.callCreateDocumentReference(any(DocumentReferenceDTO.class))).willReturn(ref);

        TSPublicationCreationResDTO result = callTSEndpoint(requestBody, pdfAttachment);
        assertNotNull(result.getTraceID());
    }


    @Test
    @DisplayName("Mining CDA error")
    void t3() { 

        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");

        TSPublicationCreationReqDTO errorRequestBody = TSPublicationCreationReqDTO.builder()
                .healthDataFormat(HealthDataFormatEnum.DICOM_SR)
                .mode(InjectionModeEnum.RESOURCE)
                .tipologiaStruttura(HealthcareFacilityEnum.PREVENZIONE)
                .regoleAccesso(java.util.Arrays.asList(EventCodeEnum.J07BX03))
                .identificativoDoc(StringUtility.generateUUID())
                .identificativoRep(StringUtility.generateUUID())
                .tipoDocumentoLivAlto(TipoDocAltoLivEnum.ESENZIONE)
                .assettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC002)
                .identificativoPaziente(randomFiscalCode())
                .dataInizioPrestazione("16522844617825")
                .dataFinePrestazione("1652284497269")
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


    private TSPublicationCreationResDTO callTSEndpoint(TSPublicationCreationReqDTO requestBody, byte[] fileByte) { 
        ObjectMapper objectMapper = new ObjectMapper(); 
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  
        String urlValidation = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath() + "/v1.0.0/ts-feeding";
 
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
        return restTemplate.postForObject(urlValidation, requestEntity, TSPublicationCreationResDTO.class);
    }
    
}
