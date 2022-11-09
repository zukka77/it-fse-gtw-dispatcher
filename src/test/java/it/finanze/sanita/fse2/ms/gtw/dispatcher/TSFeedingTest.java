/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.Disabled;
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
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.TSPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TSPublicationCreationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.TransformResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthDataFormatEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthcareFacilityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PracticeSettingCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.TipoDocAltoLivEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.FileUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "kafka.topic=junit-ts")
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
@Slf4j
@Disabled("Ts feeding is not ready for Sprint 6")
class TSFeedingTest extends AbstractTest {

	@Autowired
    ServletWebServerApplicationContext webServerAppCtxt;

    @Autowired
	RestTemplate restTemplate;

    @MockBean
	FhirMappingClient fhirMappingClient;

    @Test
    @DisplayName("Happy path")
    void happyPath() {

        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");

        // mock fhir mapping call
        TransformResDTO ref = new TransformResDTO();
		ref.setErrorMessage("");
		ref.setJson(Document.parse("{\"json\" : \"json\"}"));
		given(fhirMappingClient.callConvertCdaInBundle(any(FhirResourceDTO.class))).willReturn(ref);

        //PublicationResultEnum result = callTSEndpoint(pdfAttachment, null);
        assertDoesNotThrow(() -> callTSEndpoint(pdfAttachment, null, true));
    }

    @Test
    @DisplayName("Force publish - skip validation")
    void forcePublish() {

        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");

        TSPublicationCreationReqDTO requestBody = buildTSRequestBody(true);

        // mock fhir mapping call
        TransformResDTO ref = new TransformResDTO();
		ref.setErrorMessage("");
		ref.setJson(Document.parse("{\"json\" : \"json\"}"));
		given(fhirMappingClient.callConvertCdaInBundle(any(FhirResourceDTO.class))).willReturn(ref);

        assertDoesNotThrow(() -> callTSEndpoint(pdfAttachment, requestBody, true));
    }

    @Test
    @DisplayName("File tests")
    void fileTests() {

        // non pdf file
    	byte[] wrongPdf = FileUtility.getFileFromInternalResources("Files/Test.docx");
        RestExecutionResultEnum resPublication = callTSEndpoint(wrongPdf, null, false);
		assertNotNull(resPublication); 
        assertEquals(RestExecutionResultEnum.DOCUMENT_TYPE_ERROR.getType(), resPublication.getType());

        // attachment pdf - wrong mode
    	byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_ATTACHMENT.pdf");
		resPublication = callTSEndpoint(pdfAttachment, buildTSRequestBody(InjectionModeEnum.RESOURCE), true);
		assertNotNull(resPublication); 
        assertEquals(RestExecutionResultEnum.MINING_CDA_ERROR.getType(), resPublication.getType());

        // attachment resource - wrong mode
    	byte[] pdfResource = FileUtility.getFileFromInternalResources("Files/resource/CDA_RESOURCE.pdf");
		resPublication = callTSEndpoint(pdfResource, buildTSRequestBody(InjectionModeEnum.ATTACHMENT), true);
		assertNotNull(resPublication); 
        assertEquals(RestExecutionResultEnum.MINING_CDA_ERROR.getType(), resPublication.getType());
        
    }


    @Test
	@DisplayName("Mandatory elements")
	void checkMandatoryElement() {

        byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/CDA_OK_SIGNED.pdf");

        // missing IdentificativoDoc        
        RestExecutionResultEnum res = callTSEndpoint(pdfAttachment, buildTSReqDTOCustom(HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, HealthcareFacilityEnum.Ospedale, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), null, 
                                                    "Identificativo rep", TipoDocAltoLivEnum.WOR, PracticeSettingCodeEnum.AD_PSC001, 
                                                    randomFiscalCode(), ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    AttivitaClinicaEnum.CON, "Identificativo Sottomissione", false), true);
        assertEquals(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());



        // missing IdentificativoRep
        res = callTSEndpoint(pdfAttachment,buildTSReqDTOCustom(HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, HealthcareFacilityEnum.Ospedale, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), "Identificativo Doc", 
                                                    null, TipoDocAltoLivEnum.WOR, PracticeSettingCodeEnum.AD_PSC001, 
                                                    randomFiscalCode(), ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    AttivitaClinicaEnum.CON, "Identificativo Sottomissione", false), true);
        assertEquals(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());


        // missing TipoDocumentoLivAlto
        res = callTSEndpoint(pdfAttachment,buildTSReqDTOCustom(HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, HealthcareFacilityEnum.Ospedale, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), "Identificativo Doc", 
                                                    "Identificativo Rep", null, PracticeSettingCodeEnum.AD_PSC001, 
                                                    randomFiscalCode(), ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    AttivitaClinicaEnum.CON, "Identificativo Sottomissione", false), true);
        assertEquals(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());


        // missing AssettoOrganizzativo
        res = callTSEndpoint(pdfAttachment,buildTSReqDTOCustom(HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, HealthcareFacilityEnum.Ospedale, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), "Identificativo Doc", 
                                                    "Identificativo Rep", TipoDocAltoLivEnum.WOR, null, 
                                                    randomFiscalCode(), ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    AttivitaClinicaEnum.CON, "Identificativo Sottomissione", false), true);
        assertEquals(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());


        // missing IdentificativoPaziente
        res = callTSEndpoint(pdfAttachment,buildTSReqDTOCustom(HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, HealthcareFacilityEnum.Ospedale, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), "Identificativo Doc", 
                                                    "Identificativo Rep", TipoDocAltoLivEnum.WOR, PracticeSettingCodeEnum.AD_PSC001, 
                                                    null, ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    AttivitaClinicaEnum.CON, "Identificativo Sottomissione", false), true);
        assertEquals(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());


        // invalid IdentificativoPaziente
        res = callTSEndpoint(pdfAttachment,buildTSReqDTOCustom(HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, HealthcareFacilityEnum.Ospedale, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), "Identificativo Doc", 
                                                    "Identificativo Rep", TipoDocAltoLivEnum.WOR, PracticeSettingCodeEnum.AD_PSC001, 
                                                    "invalid identificativo paziente", ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    AttivitaClinicaEnum.CON, "Identificativo Sottomissione", false), true);
        assertEquals(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());


        // missing TipoAttivitaClinica
        res = callTSEndpoint(pdfAttachment,buildTSReqDTOCustom(HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, HealthcareFacilityEnum.Ospedale, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), "Identificativo Doc", 
                                                    "Identificativo Rep", TipoDocAltoLivEnum.WOR, PracticeSettingCodeEnum.AD_PSC001, 
                                                    randomFiscalCode(), ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    null, "Identificativo Sottomissione", false), true);
        assertEquals(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());


        // missing IdentificativoSottomissione
        res = callTSEndpoint(pdfAttachment,buildTSReqDTOCustom(HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, HealthcareFacilityEnum.Ospedale, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), "Identificativo Doc", 
                                                    "Identificativo Rep", TipoDocAltoLivEnum.WOR, PracticeSettingCodeEnum.AD_PSC001, 
                                                    randomFiscalCode(), ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    AttivitaClinicaEnum.CON, null, false), true);
        assertEquals(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());
        
     // missing Mode - (not mandatory)
        res = callTSEndpoint(pdfAttachment,buildTSReqDTOCustom(HealthDataFormatEnum.CDA, null, HealthcareFacilityEnum.Ospedale, 
                                                    java.util.Arrays.asList(EventCodeEnum._94503_0), "Identificativo Doc", 
                                                    "Identificativo Rep", TipoDocAltoLivEnum.WOR, PracticeSettingCodeEnum.AD_PSC001, 
                                                    randomFiscalCode(), ""+new Date().getTime(), ""+new Date().getTime(), "Conservazione sostitutiva",
                                                    AttivitaClinicaEnum.CON, "Identificativo Sottomissione", false), true);
        assertNotEquals(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getType(), res.getType());
	}


    private TSPublicationCreationReqDTO buildTSReqDTOCustom(HealthDataFormatEnum healthDataFormat,
            InjectionModeEnum mode, HealthcareFacilityEnum tipologiaStruttura, List<EventCodeEnum> regoleAccesso,
            String identificativoDoc, String identificativoRep, TipoDocAltoLivEnum tipoDocumentoLivAlto,
            PracticeSettingCodeEnum assettoOrganizzativo, String identificativoPaziente, String dataInizioPrestazione,
            String dataFinePrestazione, String conservazioneSostitutiva, AttivitaClinicaEnum tipoAttivitaClinica,
            String identificativoSottomissione, boolean forcePublish) {
    	
    	List<String> regAcc = new ArrayList<>();
    	for(EventCodeEnum en : regoleAccesso) {
    		regAcc.add(en.getCode());
    	}
        return TSPublicationCreationReqDTO.builder()
                .healthDataFormat(healthDataFormat)
                .mode(mode)
                .tipologiaStruttura(tipologiaStruttura)
                .regoleAccesso(regAcc)
                .identificativoDoc(identificativoDoc)
                .identificativoRep(identificativoRep)
                .tipoDocumentoLivAlto(tipoDocumentoLivAlto)
                .assettoOrganizzativo(assettoOrganizzativo)
                .identificativoPaziente(identificativoPaziente)
                .dataInizioPrestazione(dataInizioPrestazione)
                .dataFinePrestazione(dataFinePrestazione)
                .conservazioneANorma(conservazioneSostitutiva)
                .tipoAttivitaClinica(tipoAttivitaClinica)
                .identificativoSottomissione(identificativoSottomissione)
                .forcePublish(forcePublish)
                .build();

    }

    public RestExecutionResultEnum callTSEndpoint(byte[] fileByte, TSPublicationCreationReqDTO reqDTO, boolean isValidFile) {

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
				map.add("requestBody", buildTSRequestBody(false));
			} else {
				map.add("requestBody", reqDTO);
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			log.info("Simulating a valid json payload");
			
			headers.set(Constants.Headers.JWT_HEADER, generateJwt(fileByte, isValidFile));

			String urlPublication = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath() + "/v1/ts-feeding";

			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

			restTemplate.exchange(urlPublication, HttpMethod.POST, requestEntity, TSPublicationCreationResDTO.class);
			return RestExecutionResultEnum.OK;
		} catch (Exception ex) {
			String message = ex.getMessage();
			Integer firstIndex = message.indexOf("{");
			Integer lastIndex = message.indexOf("}");
			String subString = message.substring(firstIndex, lastIndex+1);

			ErrorResponseDTO errorClass = StringUtility.fromJSON(subString, ErrorResponseDTO.class);
			output = RestExecutionResultEnum.get(errorClass.getType());
			log.info("Status {}", errorClass.getStatus());
			log.error("Error : " + ex.getMessage());
		}
		return output;
	}


    private TSPublicationCreationReqDTO buildTSRequestBody(boolean forcePublish) {

        return TSPublicationCreationReqDTO.builder()
                .healthDataFormat(HealthDataFormatEnum.CDA)
                .mode(InjectionModeEnum.ATTACHMENT)
                .tipologiaStruttura(HealthcareFacilityEnum.Ospedale)
                .regoleAccesso(java.util.Arrays.asList(EventCodeEnum.P99.getCode()))
                .identificativoDoc(StringUtility.generateUUID())
                .identificativoRep(StringUtility.generateUUID())
                .tipoDocumentoLivAlto(TipoDocAltoLivEnum.WOR)
                .assettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001)
                .identificativoPaziente(randomFiscalCode())
                .dataInizioPrestazione("1652284461782")
                .dataFinePrestazione("1652284497269")
                .conservazioneANorma("string")
                .tipoAttivitaClinica(AttivitaClinicaEnum.PHR)
                .identificativoSottomissione(StringUtility.generateUUID())
                .forcePublish(forcePublish)
                .build();

    }


    private TSPublicationCreationReqDTO buildTSRequestBody(InjectionModeEnum mode) {

        return TSPublicationCreationReqDTO.builder()
                .healthDataFormat(HealthDataFormatEnum.CDA)
                .mode(mode)
                .tipologiaStruttura(HealthcareFacilityEnum.Ospedale)
                .regoleAccesso(java.util.Arrays.asList(EventCodeEnum.P99.getCode()))
                .identificativoDoc(StringUtility.generateUUID())
                .identificativoRep(StringUtility.generateUUID())
                .tipoDocumentoLivAlto(TipoDocAltoLivEnum.WOR)
                .assettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001)
                .identificativoPaziente(randomFiscalCode())
                .dataInizioPrestazione("1652284461782")
                .dataFinePrestazione("1652284497269")
                .conservazioneANorma("string")
                .tipoAttivitaClinica(AttivitaClinicaEnum.PHR)
                .identificativoSottomissione(StringUtility.generateUUID())
                .forcePublish(false)
                .build();

    }
    
 
    
	protected ResponseEntity<TSPublicationCreationResDTO> callPlainTSFeeding(final String jwtToken, final byte[] fileByte, 
			final TSPublicationCreationReqDTO requestBody) {
			
			String urlPublication = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath() + "/v1/ts-feeding";

			LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
			ByteArrayResource fileAsResource = new ByteArrayResource(fileByte){
				@Override
				public String getFilename(){
					return "file";
				}
			};

			map.add("file", fileAsResource);
			map.add("requestBody", requestBody);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			headers.set(Constants.Headers.JWT_HEADER, jwtToken);

			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
			return restTemplate.exchange(urlPublication, HttpMethod.POST, requestEntity, TSPublicationCreationResDTO.class);
		}
    
    @Test
    void warningTSfeedingTest() {
        final byte[] file = FileUtility.getFileFromInternalResources("Files" + File.separator + "attachment" + File.separator + "CDA_OK_SIGNED.pdf");
		final String jwtToken = generateJwt(file, true);
		
        TSPublicationCreationReqDTO requestBody = buildTSRequestBody(null);
        
        // mock fhir mapping call
        TransformResDTO ref = new TransformResDTO();
		ref.setErrorMessage("");
		ref.setJson(Document.parse("{\"json\" : \"json\"}"));
		
		given(fhirMappingClient.callConvertCdaInBundle(any(FhirResourceDTO.class))).willReturn(ref);
		
		final ResponseEntity<TSPublicationCreationResDTO> responseTSfeeding = callPlainTSFeeding(jwtToken, file, requestBody);

		assertDoesNotThrow(() -> callTSEndpoint(file, requestBody, true));
		assertEquals(Constants.Misc.WARN_EXTRACTION_SELECTION, responseTSfeeding.getBody().getWarning());
    }

}
