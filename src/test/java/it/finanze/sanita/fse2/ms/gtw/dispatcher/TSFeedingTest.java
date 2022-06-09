package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Date;
import java.util.List;

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

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.FhirMappingClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.TSPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TSPublicationCreationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.DocumentReferenceResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthDataFormatEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthcareFacilityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PracticeSettingCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PublicationResultEnum;
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
    void happyPath() {

        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");

        // mock fhir mapping call
        DocumentReferenceResDTO ref = new DocumentReferenceResDTO();
		ref.setErrorMessage("");
		ref.setJson("{\"json\" : \"json\"}");
		given(fhirMappingClient.callCreateDocumentReference(any(DocumentReferenceDTO.class))).willReturn(ref);

        //PublicationResultEnum result = callTSEndpoint(pdfAttachment, null);
        assertDoesNotThrow(() -> callTSEndpoint(pdfAttachment, null));
    }

    @Test
    @DisplayName("Force publish - skip validation")
    void forcePublish() {

        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");

        TSPublicationCreationReqDTO requestBody = buildTSRequestBody(true);

        // mock fhir mapping call
        DocumentReferenceResDTO ref = new DocumentReferenceResDTO();
		ref.setErrorMessage("");
		ref.setJson("{\"json\" : \"json\"}");
		given(fhirMappingClient.callCreateDocumentReference(any(DocumentReferenceDTO.class))).willReturn(ref);

        assertDoesNotThrow(() -> callTSEndpoint(pdfAttachment, requestBody));
    }

    @Test
    @DisplayName("File tests")
    void fileTests() {

        // non pdf file
    	byte[] wrongPdf = FileUtility.getFileFromInternalResources("Files/Test.docx");
        PublicationResultEnum resPublication = callTSEndpoint(wrongPdf, null);
		assertNotNull(resPublication); 
        assertEquals(PublicationResultEnum.DOCUMENT_TYPE_ERROR.getType(), resPublication.getType());

        // attachment pdf - wong mode
    	byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_ATTACHMENT.pdf");
		resPublication = callTSEndpoint(pdfAttachment, buildTSRequestBody(InjectionModeEnum.RESOURCE));
		assertNotNull(resPublication); 
        assertEquals(PublicationResultEnum.MINING_CDA_ERROR.getType(), resPublication.getType());

        // attachment resource - wong mode
    	byte[] pdfResource = FileUtility.getFileFromInternalResources("Files/resource/CDA_RESOURCE.pdf");
		resPublication = callTSEndpoint(pdfResource, buildTSRequestBody(InjectionModeEnum.ATTACHMENT));
		assertNotNull(resPublication); 
        assertEquals(PublicationResultEnum.MINING_CDA_ERROR.getType(), resPublication.getType());

    }


    @Test
	@DisplayName("Mandatory elements")
	void checkMandatoryElement() {

        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");

        // missing IdentificativoDoc        
        PublicationResultEnum res = callTSEndpoint(pdfAttachment,buildTSReqDTOCustom(HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, HealthcareFacilityEnum.OSPEDALE, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), null, 
                                                    "Identificativo rep", TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW, PracticeSettingCodeEnum.AD_PSC001, 
                                                    randomFiscalCode(), ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    AttivitaClinicaEnum.CONSULTO, "Identificativo Sottomissione", false));
        assertEquals(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());



        // missing IdentificativoRep
        res = callTSEndpoint(pdfAttachment,buildTSReqDTOCustom(HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, HealthcareFacilityEnum.OSPEDALE, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), "Identificativo Doc", 
                                                    null, TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW, PracticeSettingCodeEnum.AD_PSC001, 
                                                    randomFiscalCode(), ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    AttivitaClinicaEnum.CONSULTO, "Identificativo Sottomissione", false));
        assertEquals(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());


        // missing TipoDocumentoLivAlto
        res = callTSEndpoint(pdfAttachment,buildTSReqDTOCustom(HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, HealthcareFacilityEnum.OSPEDALE, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), "Identificativo Doc", 
                                                    "Identificativo Rep", null, PracticeSettingCodeEnum.AD_PSC001, 
                                                    randomFiscalCode(), ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    AttivitaClinicaEnum.CONSULTO, "Identificativo Sottomissione", false));
        assertEquals(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());


        // missing AssettoOrganizzativo
        res = callTSEndpoint(pdfAttachment,buildTSReqDTOCustom(HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, HealthcareFacilityEnum.OSPEDALE, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), "Identificativo Doc", 
                                                    "Identificativo Rep", TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW, null, 
                                                    randomFiscalCode(), ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    AttivitaClinicaEnum.CONSULTO, "Identificativo Sottomissione", false));
        assertEquals(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());


        // missing IdentificativoPaziente
        res = callTSEndpoint(pdfAttachment,buildTSReqDTOCustom(HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, HealthcareFacilityEnum.OSPEDALE, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), "Identificativo Doc", 
                                                    "Identificativo Rep", TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW, PracticeSettingCodeEnum.AD_PSC001, 
                                                    null, ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    AttivitaClinicaEnum.CONSULTO, "Identificativo Sottomissione", false));
        assertEquals(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());


        // invalid IdentificativoPaziente
        res = callTSEndpoint(pdfAttachment,buildTSReqDTOCustom(HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, HealthcareFacilityEnum.OSPEDALE, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), "Identificativo Doc", 
                                                    "Identificativo Rep", TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW, PracticeSettingCodeEnum.AD_PSC001, 
                                                    "invalid identificativo paziente", ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    AttivitaClinicaEnum.CONSULTO, "Identificativo Sottomissione", false));
        assertEquals(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());


        // missing TipoAttivitaClinica
        res = callTSEndpoint(pdfAttachment,buildTSReqDTOCustom(HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, HealthcareFacilityEnum.OSPEDALE, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), "Identificativo Doc", 
                                                    "Identificativo Rep", TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW, PracticeSettingCodeEnum.AD_PSC001, 
                                                    randomFiscalCode(), ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    null, "Identificativo Sottomissione", false));
        assertEquals(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());


        // missing IdentificativoSottomissione
        res = callTSEndpoint(pdfAttachment,buildTSReqDTOCustom(HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, HealthcareFacilityEnum.OSPEDALE, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), "Identificativo Doc", 
                                                    "Identificativo Rep", TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW, PracticeSettingCodeEnum.AD_PSC001, 
                                                    randomFiscalCode(), ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    AttivitaClinicaEnum.CONSULTO, null, false));
        assertEquals(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());
	}


    private TSPublicationCreationReqDTO buildTSReqDTOCustom(HealthDataFormatEnum healthDataFormat,
            InjectionModeEnum mode, HealthcareFacilityEnum tipologiaStruttura, List<EventCodeEnum> regoleAccesso,
            String identificativoDoc, String identificativoRep, TipoDocAltoLivEnum tipoDocumentoLivAlto,
            PracticeSettingCodeEnum assettoOrganizzativo, String identificativoPaziente, String dataInizioPrestazione,
            String dataFinePrestazione, String conservazioneSostitutiva, AttivitaClinicaEnum tipoAttivitaClinica,
            String identificativoSottomissione, boolean forcePublish) {
        return TSPublicationCreationReqDTO.builder()
                .healthDataFormat(healthDataFormat)
                .mode(mode)
                .tipologiaStruttura(tipologiaStruttura)
                .regoleAccesso(regoleAccesso)
                .identificativoDoc(identificativoDoc)
                .identificativoRep(identificativoRep)
                .tipoDocumentoLivAlto(tipoDocumentoLivAlto)
                .assettoOrganizzativo(assettoOrganizzativo)
                .identificativoPaziente(identificativoPaziente)
                .dataInizioPrestazione(dataInizioPrestazione)
                .dataFinePrestazione(dataFinePrestazione)
                .conservazioneSostitutiva(conservazioneSostitutiva)
                .tipoAttivitaClinica(tipoAttivitaClinica)
                .identificativoSottomissione(identificativoSottomissione)
                .forcePublish(forcePublish)
                .build();

    }

    public PublicationResultEnum callTSEndpoint(byte[] fileByte, TSPublicationCreationReqDTO reqDTO) {

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
				map.add("requestBody", buildTSRequestBody(false));
			} else {
				map.add("requestBody", reqDTO);
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			log.info("Simulating a valid json payload");
			
			headers.set(Constants.Headers.JWT_HEADER, generateJwt(StringUtility.encodeSHA256(fileByte)));

			String urlPublication = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath() + "/v1.0.0/ts-feeding";

			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

			ResponseEntity<TSPublicationCreationResDTO> response = restTemplate.exchange(urlPublication, HttpMethod.POST, requestEntity, TSPublicationCreationResDTO.class);
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


    private TSPublicationCreationReqDTO buildTSRequestBody(boolean forcePublish) {

        return TSPublicationCreationReqDTO.builder()
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
                .forcePublish(forcePublish)
                .build();

    }


    private TSPublicationCreationReqDTO buildTSRequestBody(InjectionModeEnum mode) {

        return TSPublicationCreationReqDTO.builder()
                .healthDataFormat(HealthDataFormatEnum.CDA)
                .mode(mode)
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

    }

    
    
}
