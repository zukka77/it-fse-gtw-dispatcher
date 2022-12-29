/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.io.File;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.FhirMappingClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.TransformResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthDataFormatEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthcareFacilityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PracticeSettingCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.TipoDocAltoLivEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDocumentReferenceSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl.IniEdsInvocationSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.FileUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class DocumentReferenceTest extends AbstractTest {

	@Autowired
	private IDocumentReferenceSRV documentReferenceSRV;
	
	@MockBean
	private FhirMappingClient client;

	@Autowired
	private IniEdsInvocationSRV iniEdsInvocationSRV;
	
	@Test
	@DisplayName("createDocumentReference OK")
	void createDocumentReferenceOkTest() {
		String workflowInstanceId = StringUtility.generateUUID();
		TransformResDTO res = new TransformResDTO();
		res.setErrorMessage("");
		res.setJson(Document.parse("{\"json\" : \"json\"}"));
		given(client.callConvertCdaInBundle(any(FhirResourceDTO.class),"")).willReturn(res);
		byte[] cdaFile = FileUtility.getFileFromInternalResources("Files" + File.separator + "Esempio CDA2_Referto Medicina di Laboratorio v6_OK.xml");
		String cda = new String(cdaFile);
		PublicationCreationReqDTO reqDTO = buildPublicationReqDTO(workflowInstanceId);
		String documentSha = StringUtility.encodeSHA256(cdaFile);
		ResourceDTO resourceDTO = documentReferenceSRV.createFhirResources(cda, reqDTO, documentSha.length(), documentSha,
				"PersonId", "",false);
		assertNotNull(resourceDTO.getDocumentEntryJson());
		assertNotNull(resourceDTO.getSubmissionSetEntryJson());
		assertNull(resourceDTO.getErrorMessage());
		
		final String jwt = generateJwt(cdaFile, true);
		String[] chunks = jwt.substring(Constants.App.BEARER_PREFIX.length()).split("\\.");
				
		final String payload = new String(Base64.getDecoder().decode(chunks[1]));

		// Building the object asserts that all required values are present
		JWTTokenDTO jwtToken = new JWTTokenDTO(JWTPayloadDTO.extractPayload(payload));

		Boolean insert = iniEdsInvocationSRV.insert(workflowInstanceId, resourceDTO, jwtToken);
		assertTrue(insert);
	}
	
	private PublicationCreationReqDTO buildPublicationReqDTO(String workflowInstanceId) {
		PublicationCreationReqDTO output = PublicationCreationReqDTO.builder().
				assettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001).
				conservazioneANorma("Conservazione").
				dataFinePrestazione(""+new Date().getTime()).
				dataInizioPrestazione(""+new Date().getTime()).
				healthDataFormat(HealthDataFormatEnum.CDA).
				identificativoDoc("Identificativo doc").
				identificativoRep("Identificativo rep").
				identificativoSottomissione("identificativo sottomissione").
				mode(InjectionModeEnum.ATTACHMENT).
				attiCliniciRegoleAccesso(Arrays.asList(EventCodeEnum._94503_0.getCode())).
				tipoAttivitaClinica(AttivitaClinicaEnum.CON).
				tipoDocumentoLivAlto(TipoDocAltoLivEnum.PRE).
				tipologiaStruttura(HealthcareFacilityEnum.Ospedale).
				workflowInstanceId(workflowInstanceId).
				build();
		return output;
	}

	@Test
	@DisplayName("createDocumentReference error")
	void createDocumentReferenceErrorTest() {
		String workflowInstanceId = StringUtility.generateUUID();
		TransformResDTO res = new TransformResDTO();
		res.setErrorMessage("errorMessage");
		given(client.callConvertCdaInBundle(any(FhirResourceDTO.class),"")).willReturn(res);
		byte[] cdaFile = FileUtility.getFileFromInternalResources("Files" + File.separator + "Esempio CDA2_Referto Medicina di Laboratorio v6_OK.xml");
		String cda = new String(cdaFile);
		PublicationCreationReqDTO reqDTO = buildPublicationReqDTO(workflowInstanceId);
		String documentSha = StringUtility.encodeSHA256(cdaFile);
		ResourceDTO resourceDTO = documentReferenceSRV.createFhirResources(cda, reqDTO, documentSha.length(), documentSha,
				"PersonId", "",false);
		ResourceDTO expectedOutputDTO = new ResourceDTO();
		expectedOutputDTO.setErrorMessage("errorMessage");
		assertEquals(expectedOutputDTO, resourceDTO);
	}

	@Test
	@DisplayName("createDocumentReferenceErrorConnectionRefused Test")
	void createDocumentReferenceConnectionRefusedTest() {
		String workflowInstanceId = StringUtility.generateUUID();
		TransformResDTO res = new TransformResDTO();
		res.setErrorMessage("");
		res.setJson(Document.parse("{\"json\" : \"json\"}"));
		given(client.callConvertCdaInBundle(any(FhirResourceDTO.class),"")).willThrow(ConnectionRefusedException.class);
		byte[] cdaFile = FileUtility.getFileFromInternalResources("Files" + File.separator + "Esempio CDA2_Referto Medicina di Laboratorio v6_OK.xml");
		String cda = new String(cdaFile);
		PublicationCreationReqDTO reqDTO = buildPublicationReqDTO(workflowInstanceId);
		String documentSha = StringUtility.encodeSHA256(cdaFile);
		assertThrows(ConnectionRefusedException.class, () -> documentReferenceSRV.createFhirResources(cda, reqDTO, documentSha.length(), documentSha,
				"PersonId", "",false));
	}

	@Test
	@DisplayName("createDocumentReferenceErrorBusinessException Test")
	void createDocumentReferenceErrorBusinessException() {
		String workflowInstanceId = StringUtility.generateUUID();
		TransformResDTO res = new TransformResDTO();
		res.setErrorMessage("");
		res.setJson(Document.parse("{\"json\" : \"json\"}"));
		given(client.callConvertCdaInBundle(any(FhirResourceDTO.class),"")).willThrow(BusinessException.class);
		byte[] cdaFile = FileUtility.getFileFromInternalResources("Files" + File.separator + "Esempio CDA2_Referto Medicina di Laboratorio v6_OK.xml");
		String cda = new String(cdaFile);
		PublicationCreationReqDTO reqDTO = buildPublicationReqDTO(workflowInstanceId);
		String documentSha = StringUtility.encodeSHA256(cdaFile);
		assertThrows(BusinessException.class, () -> documentReferenceSRV.createFhirResources(cda, reqDTO, documentSha.length(), documentSha,
				"PersonId", "",false));
	}
}
