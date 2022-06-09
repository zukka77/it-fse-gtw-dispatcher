package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ValidationCDAResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthDataFormatEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PracticeSettingCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.TipoDocAltoLivEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ValidationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.ICdaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.FileUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
public class ValidationTest extends AbstractTest {


	@Autowired
	private ICdaSRV cdaSRV;


	/**
	 * Calcolo performance: numero di prove per singolo utente.
	 */
	private static final int N_PROVE = 50;

	/**
	 * Calcolo performance: numero utenti.
	 */
	private static final int N_UTENTI = 20;
	

	/**
	 * Numero di verifiche di raggiungimento del numero di test necessari.
	 */
	private static final int SLEEP_TRY = 500;

	/**
	 * Costante utilizzata per i calcoli del throughput.
	 */
	private static final int MILLISECONDS_IN_S = 1000;
	
	/**
	 * Quanto di tempo di attesa tra due check adiacenti per comprendere se si è raggiunto il numero di test necessari.
	 */
	private static final int SLEEP_TIME = 100;
	
    @Test
    @DisplayName("Wrong File Test")
    void wrongFileTest() {
    	byte[] docxByte = FileUtility.getFileFromInternalResources("Files" + File.separator + "Test.docx");

    	//invio un non pdf -
    	Map<String, ValidationResultEnum> result = callValidation(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, docxByte,true);
    	assertEquals(ValidationResultEnum.DOCUMENT_TYPE_ERROR, result.values().iterator().next());

    	byte[] pdfByte = FileUtility.getFileFromInternalResources("Files" + File.separator + "Test.pdf");
    	result = callValidation(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdfByte,true);
    	assertEquals(ValidationResultEnum.MINING_CDA_ERROR, result.values().iterator().next());
    }

    @Test
    @DisplayName("Validation Test")
    void validationTest() {
    	
    	byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/pdf_msg_SATLED_LED_Lettera_di_Dimissione.pdf");
    	byte[] pdfResource = FileUtility.getFileFromInternalResources("Files/resource/cert1.pdf");
    	
    	 Map<String, ValidationResultEnum> result = callValidation(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdfResource,true);
    	 assertNotNull(result);
		 assertEquals(ValidationResultEnum.MINING_CDA_ERROR, result.values().iterator().next());
    	
    	 result = callValidation(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.RESOURCE, pdfAttachment,true);
    	 assertEquals(ValidationResultEnum.MINING_CDA_ERROR, result.values().iterator().next());
    	
    	 result = callValidation(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdfAttachment,true);
    	 assertEquals(ValidationResultEnum.OK, result.values().iterator().next());
    	 assertNull(cdaSRV.get(result.keySet().iterator().next()), "La transazione non deve essere presente.");
    	
    	 result = callValidation(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.RESOURCE, pdfResource,true);
    	 assertEquals(ValidationResultEnum.OK, result.values().iterator().next());
    	 assertNull(cdaSRV.get(result.keySet().iterator().next()), "La transazione non deve essere presente.");

    	 result = callValidation(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, null, pdfAttachment,true);
    	 assertEquals(ValidationResultEnum.OK, result.values().iterator().next());
    	 assertNull(cdaSRV.get(result.keySet().iterator().next()), "La transazione non deve essere presente.");

    	 result = callValidation(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, null, pdfResource,true);
    	 assertEquals(ValidationResultEnum.OK, result.values().iterator().next());
    	 assertNull(cdaSRV.get(result.keySet().iterator().next()), "La transazione non deve essere presente.");
    }

    @Test
    @DisplayName("Pre Publish Test")
    void prePublishTest() {
  
    	byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/pdf_msg_SATLED_LED_Lettera_di_Dimissione.pdf");
    	byte[] pdfResource = FileUtility.getFileFromInternalResources("Files/resource/cert1.pdf");
    	
    	Map<String, ValidationResultEnum> result = callValidation(ActivityEnum.PRE_PUBLISHING, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdfResource,true);
    	assertEquals(ValidationResultEnum.MINING_CDA_ERROR, result.values().iterator().next());
    	
    	result = callValidation(ActivityEnum.PRE_PUBLISHING, HealthDataFormatEnum.CDA, InjectionModeEnum.RESOURCE, pdfAttachment,true);
    	assertEquals(ValidationResultEnum.MINING_CDA_ERROR, result.values().iterator().next());
    	
    	result = callValidation(ActivityEnum.PRE_PUBLISHING, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdfAttachment,true);
    	assertEquals(ValidationResultEnum.OK, result.values().iterator().next());
    	assertNotNull(cdaSRV.get(result.keySet().iterator().next()), "La transazione non deve essere presente.");
    	
    	result = callValidation(ActivityEnum.PRE_PUBLISHING, HealthDataFormatEnum.CDA, InjectionModeEnum.RESOURCE, pdfResource,true);
    	assertEquals(ValidationResultEnum.OK, result.values().iterator().next());
    	assertNotNull(cdaSRV.get(result.keySet().iterator().next()), "La transazione non deve essere presente.");

    	result = callValidation(ActivityEnum.PRE_PUBLISHING, HealthDataFormatEnum.CDA, null, pdfAttachment,true);
    	assertEquals(ValidationResultEnum.OK, result.values().iterator().next());
    	assertNotNull(cdaSRV.get(result.keySet().iterator().next()), "La transazione non deve essere presente.");

    	result = callValidation(ActivityEnum.PRE_PUBLISHING, HealthDataFormatEnum.CDA, null, pdfResource,true);
    	assertEquals(ValidationResultEnum.OK, result.values().iterator().next());
    	assertNotNull(cdaSRV.get(result.keySet().iterator().next()), "La transazione non deve essere presente.");
    	 
    }
	
	@Test
	@DisplayName("Mandatory elements")
	void checkMandatoryElement() {
		byte[] pdfAttachment = FileUtility.getFileFromInternalResources("Files/attachment/pdf_msg_SATLED_LED_Lettera_di_Dimissione.pdf");
	 
		Map<String,ValidationResultEnum> validationResult = callValidation(null, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdfAttachment,true);
		assertEquals(ValidationResultEnum.MANDATORY_ELEMENT_ERROR, validationResult.get("ERROR"));
		
		validationResult = callValidation(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdfAttachment,true,
				buildValidationReqDTOCustom(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, 
						null, null, null, null, null));
		assertEquals(ValidationResultEnum.MANDATORY_ELEMENT_ERROR, validationResult.get("ERROR"));
		
		validationResult = callValidation(null, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdfAttachment,true, 
				buildValidationReqDTOCustom(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, 
						TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW, null, null, null, null));
		assertEquals(ValidationResultEnum.MANDATORY_ELEMENT_ERROR, validationResult.get("ERROR"));
		
		validationResult = callValidation(null, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdfAttachment,true,
				buildValidationReqDTOCustom(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, 
						TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW, PracticeSettingCodeEnum.AD_PSC001, null, null, null));
		assertEquals(ValidationResultEnum.MANDATORY_ELEMENT_ERROR, validationResult.get("ERROR"));
		
		validationResult = callValidation(null, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdfAttachment,true,
				buildValidationReqDTOCustom(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, 
						TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW, PracticeSettingCodeEnum.AD_PSC001, randomFiscalCode(), null, "tipoIdentificativoPaziente"));
		assertEquals(ValidationResultEnum.MANDATORY_ELEMENT_ERROR, validationResult.get("ERROR"));
		
		validationResult = callValidation(null, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdfAttachment,true,
				buildValidationReqDTOCustom(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, 
						TipoDocAltoLivEnum.DOCUMENTO_WORKFLOW, PracticeSettingCodeEnum.AD_PSC001, randomFiscalCode(), AttivitaClinicaEnum.CONSULTO
						, null));
		assertEquals(ValidationResultEnum.MANDATORY_ELEMENT_ERROR, validationResult.get("ERROR"));

	}
	
	/**
	 * Valutazione performance.
	 */
	@Test
	@Disabled("Used to evalate performance, does not asser anything")
	void performance() {  
		final Collection<Long> syncOK = Collections.synchronizedCollection(new ArrayList<>());
		final Collection<Long> syncKO = Collections.synchronizedCollection(new ArrayList<>());
		final Long start = new Date().getTime();
		int u = 0;
		
		byte[] pdf = FileUtility.getFileFromInternalResources("CDA_OK_SIGNED.pdf");
		for (; u < N_UTENTI; u++) {
			log.info("START USER: " + u);
			startUser(syncOK, syncKO,pdf);
		}
		
		wait(syncOK, syncKO);
		final Long stop = new Date().getTime();
		final BigDecimal bdSumTime = new BigDecimal(stop - start).divide(new BigDecimal(MILLISECONDS_IN_S), 2, RoundingMode.HALF_UP);

		final BigDecimal nOkReq = BigDecimal.valueOf(syncOK.size());
		if (nOkReq.compareTo(BigDecimal.ZERO) > 0) {
			log.error("REQ/S: " + BigDecimal.valueOf(syncOK.size()).divide(bdSumTime, 2, RoundingMode.HALF_UP).toString(), new Exception());
		}
		assertEquals(u, N_UTENTI);
	}
	
	private void wait(final Collection<Long> syncOK, final Collection<Long> syncKO) {
		Integer nSleepTime = 0;
		for (;;) {
			try {
				final Integer nSamples = syncOK.size() + syncKO.size();
				final Integer nNeededSamples = N_UTENTI * N_PROVE;
				Thread.sleep(SLEEP_TIME);
				if ((nSamples.equals(nNeededSamples)) || (nSleepTime > SLEEP_TRY)) {
					break;
				}
				nSleepTime++;
			} catch (final InterruptedException e) {
				log.error(""+e);
				throw new RuntimeException(e);
			}
		}
	}
	
	private void startUser(final Collection<Long> syncOK, final Collection<Long> syncKO, byte[] pdf) {
		new Thread() {
			@Override
			public void run() {
				for (int x = 0; x < N_PROVE; x++) { 
					boolean fail = true;
					try {
						final Map<String, ValidationResultEnum> resp = callValidation(ActivityEnum.VALIDATION, HealthDataFormatEnum.CDA, InjectionModeEnum.ATTACHMENT, pdf,true);
						if (ValidationResultEnum.OK.equals(resp.values().iterator().next())) {
							fail = false;
						}
					} catch (final Exception e) {
						log.error(""+e);
					}
					if (fail) {
						log.info("SAMPLE KO");
						syncKO.add(1L);
					} else {
						log.info("SAMPLE OK");
						syncOK.add(1L);
					}
				}
			}
			
		}.start();

	}

	@Test
	void validationErrorTest() {

		final byte[] file = FileUtility.getFileFromInternalResources("Files" + File.separator + "attachment" + File.separator + "CDA_OK_SIGNED.pdf");

		final String jwtToken = generateJwt(StringUtility.encodeSHA256(file));
		ValidationCDAReqDTO requestBody = new ValidationCDAReqDTO();
		Exception thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainValidation(jwtToken, file, requestBody));
		assertTrue(thrownException.getMessage().contains(ValidationResultEnum.MANDATORY_ELEMENT_ERROR.getType()));
	
		requestBody.setActivity(ActivityEnum.PRE_PUBLISHING);
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainValidation(jwtToken, file, requestBody));
		assertTrue(thrownException.getMessage().contains(ValidationResultEnum.MANDATORY_ELEMENT_ERROR.getType()));

		requestBody.setTipoDocumentoLivAlto(TipoDocAltoLivEnum.PRESCRIZIONE);
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainValidation(jwtToken, file, requestBody));
		assertTrue(thrownException.getMessage().contains(ValidationResultEnum.MANDATORY_ELEMENT_ERROR.getType()));

		requestBody.setTipoAttivitaClinica(AttivitaClinicaEnum.CONSULTO);
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainValidation(jwtToken, file, requestBody));
		assertTrue(thrownException.getMessage().contains(ValidationResultEnum.MANDATORY_ELEMENT_ERROR.getType()));

		requestBody.setIdentificativoSottomissione("identificativoSottomissione");
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainValidation(jwtToken, file, requestBody));
		assertTrue(thrownException.getMessage().contains(ValidationResultEnum.MANDATORY_ELEMENT_ERROR.getType()));
		
		requestBody.setAssettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001);
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainValidation(jwtToken, file, requestBody));
		assertTrue(thrownException.getMessage().contains(ValidationResultEnum.MANDATORY_ELEMENT_ERROR.getType()));
		
		requestBody.setIdentificativoPaziente("INVALID FISCAL CODE");
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainValidation(jwtToken, file, requestBody));
		assertTrue(thrownException.getMessage().contains(ValidationResultEnum.MANDATORY_ELEMENT_ERROR.getType()));
		
		requestBody.setIdentificativoPaziente(randomFiscalCode());
		final ResponseEntity<ValidationCDAResDTO> validationResponse = callPlainValidation(jwtToken, file, requestBody);
		assertEquals(HttpStatus.SC_CREATED, validationResponse.getStatusCode().value());
		assertNotNull(validationResponse.getBody());
		assertNotNull(validationResponse.getBody().getWorkflowInstanceId());

		requestBody.setActivity(ActivityEnum.VALIDATION);
		final ResponseEntity<ValidationCDAResDTO> validationResponse200 = callPlainValidation(jwtToken, file, requestBody);
		assertNotNull(validationResponse200.getBody());
		assertNotNull(validationResponse200.getBody().getWorkflowInstanceId());

		thrownException = assertThrows(HttpClientErrorException.Unauthorized.class, () -> callPlainValidation(null, file, requestBody));
		
		thrownException = assertThrows(HttpClientErrorException.BadRequest.class, () -> callPlainValidation(jwtToken, new byte[0], requestBody));
		assertTrue(thrownException.getMessage().contains(ValidationResultEnum.EMPTY_FILE_ERROR.getType()));
	}

}
