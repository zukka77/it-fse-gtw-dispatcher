package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.io.File;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.FhirMappingClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTHeaderDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.DocumentReferenceResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthDataFormatEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthcareFacilityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PracticeSettingCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.TipoDocAltoLivEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDocumentReferenceSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl.IniEdsInvocationSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.FileUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
public class DocumentReferenceTest extends AbstractTest {

	@Autowired
	private IDocumentReferenceSRV documentReferenceSRV;
	
	@MockBean
	private FhirMappingClient client;

	@Autowired
	private IniEdsInvocationSRV iniEdsInvocationSRV;
	
	@Test
	void createDocumentReference() {
		
		String transactionId = StringUtility.generateUUID();
		DocumentReferenceResDTO res = new DocumentReferenceResDTO();
		res.setErrorMessage("");
		res.setJson("{\"json\" : \"json\"}");
		given(client.callCreateDocumentReference(any(DocumentReferenceDTO.class))).willReturn(res);
		byte[] cdaFile = FileUtility.getFileFromInternalResources("Files" + File.separator + "Esempio CDA2_Referto Medicina di Laboratorio v6_OK.xml");
		String cda = new String(cdaFile);
		PublicationCreationReqDTO reqDTO = buildPublicationReqDTO(transactionId);
		String documentSha = StringUtility.encodeSHA256(cdaFile);
		FhirResourceDTO resourceDTO = documentReferenceSRV.createFhirResources(cda, reqDTO, documentSha.length(), documentSha,null);
		assertNotNull(resourceDTO.getDocumentEntryJson());
		assertNotNull(resourceDTO.getDocumentReferenceJson());
		assertNotNull(resourceDTO.getSubmissionSetEntryJson());
		assertNull(resourceDTO.getErrorMessage());
		
		final String jwt = generateJwt(documentSha);
		String[] chunks = jwt.substring(Constants.App.BEARER_PREFIX.length()).split("\\.");
				
		final String header = new String(Base64.getDecoder().decode(chunks[0]));
		final String payload = new String(Base64.getDecoder().decode(chunks[1]));

		// Building the object asserts that all required values are present
		JWTTokenDTO jwtToken = new JWTTokenDTO(JWTHeaderDTO.extractHeader(header), JWTPayloadDTO.extractPayload(payload));

		Boolean insert = iniEdsInvocationSRV.insert(transactionId, resourceDTO, jwtToken);
		assertTrue(insert);
	}
	
	private PublicationCreationReqDTO buildPublicationReqDTO(String transactionId) {
		PublicationCreationReqDTO output = PublicationCreationReqDTO.builder().
				assettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001).
				conservazioneSostitutiva("Conservazione").
				dataFinePrestazione(""+new Date().getTime()).
				dataInizioPrestazione(""+new Date().getTime()).
				forcePublish(false).
				healthDataFormat(HealthDataFormatEnum.CDA).
				identificativoDoc("Identificativo doc").
				identificativoRep("Identificativo rep").
				identificativoSottomissione("identificativo sottomissione").
				mode(InjectionModeEnum.ATTACHMENT).
				attiCliniciRegoleAccesso(Arrays.asList(EventCodeEnum._94503_0)).
				tipoAttivitaClinica(AttivitaClinicaEnum.CON).
				tipoDocumentoLivAlto(TipoDocAltoLivEnum.PRE).
				tipologiaStruttura(HealthcareFacilityEnum.Ospedale).
				workflowInstanceId(transactionId).
				build();
		return output;
	}
	
}
