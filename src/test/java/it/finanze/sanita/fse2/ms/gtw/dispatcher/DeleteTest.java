/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniTraceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.INIErrorEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
class DeleteTest extends AbstractTest {

	@Autowired
	ServletWebServerApplicationContext webServerAppCtxt;

	@SpyBean
	RestTemplate restTemplate;

	@Test
	@DisplayName("Delete of a document")
	void givenAnIdentifier_shouldExecuteDeletion() {

		final String idDocument = StringUtility.generateUUID();
		mockEdsClient(HttpStatus.OK);
		mockIniClient(HttpStatus.OK, true);

		final ResponseEntity<ResponseDTO> response = callDelete(idDocument);
		final ResponseDTO body = response.getBody();

		assertNotNull(body, "A response body should have been returned");
		assertNotNull(body.getSpanID(), "A span ID should have been returned");
		assertNotNull(body.getTraceID(), "A trace ID should have been returned");
	}

	@Test
	@DisplayName("INI Error returned")
	void whenIniFails_anErrorShouldBeReturned() {
		final String idDocument = StringUtility.generateUUID();

		mockIniClient(HttpStatus.INTERNAL_SERVER_ERROR, false);
		mockEdsClient(HttpStatus.OK);
		assertThrows(HttpServerErrorException.InternalServerError.class, () -> callDelete(idDocument));
	}

	@Test
	@DisplayName("Not found INI Error returned")
	void whenIniFailsForNotFound_anErrorShouldBeReturned() {
		final String idDocument = StringUtility.generateUUID();

		mockIniClient(HttpStatus.NOT_FOUND, false);
		mockEdsClient(HttpStatus.OK);
		assertThrows(HttpClientErrorException.NotFound.class, () -> callDelete(idDocument));
	}

	@Test
	@DisplayName("Generic INI Error returned")
	void whenIniFailsForGenericHttp_anErrorShouldBeReturned() {
		final String idDocument = StringUtility.generateUUID();

		mockIniClient(HttpStatus.BAD_REQUEST, false);
		mockEdsClient(HttpStatus.OK);
		assertThrows(HttpServerErrorException.InternalServerError.class, () -> callDelete(idDocument));
	}

	@Test
	@DisplayName("EDS Error returned")
	void whenEdsFails_anErrorShouldBeReturned() {

		final String idDocument = StringUtility.generateUUID();

		mockIniClient(HttpStatus.OK, true);
		mockEdsClient(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThrows(HttpServerErrorException.InternalServerError.class, () -> callDelete(idDocument));
	}

	void mockIniClient(final HttpStatus status, boolean esito) {
		log.info("Mocking INI client");

		if (status.is2xxSuccessful() && esito) {
			IniTraceResponseDTO response = new IniTraceResponseDTO();
			response.setSpanID(StringUtility.generateUUID());
			response.setTraceID(StringUtility.generateUUID());
			response.setEsito(true);
			response.setErrorMessage(null);
			Mockito.doReturn(new ResponseEntity<>(response, status)).when(restTemplate).exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), ArgumentMatchers.eq(IniTraceResponseDTO.class));
		} else if (status.equals(HttpStatus.NOT_FOUND) && !esito) {
			IniTraceResponseDTO response = new IniTraceResponseDTO();
			response.setSpanID(StringUtility.generateUUID());
			response.setTraceID(StringUtility.generateUUID());
			response.setEsito(false);
			response.setErrorMessage(INIErrorEnum.RECORD_NOT_FOUND.toString());
			Mockito.doReturn(new ResponseEntity<>(response, HttpStatus.OK)).when(restTemplate).exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), ArgumentMatchers.eq(IniTraceResponseDTO.class));
		} else if (status.equals(HttpStatus.BAD_REQUEST) && !esito) {
			IniTraceResponseDTO response = new IniTraceResponseDTO();
			response.setSpanID(StringUtility.generateUUID());
			response.setTraceID(StringUtility.generateUUID());
			response.setEsito(false);
			response.setErrorMessage("Generic error from INI");
			Mockito.doReturn(new ResponseEntity<>(response, HttpStatus.OK)).when(restTemplate).exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), ArgumentMatchers.eq(IniTraceResponseDTO.class));
		} else if (status.is4xxClientError()) {
			Mockito.doThrow(new RestClientException("")).when(restTemplate).exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), ArgumentMatchers.eq(IniTraceResponseDTO.class));
		} else {
			Mockito.doThrow(new BusinessException("")).when(restTemplate).exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), ArgumentMatchers.eq(IniTraceResponseDTO.class));
		}
	}


	void mockEdsClient(final HttpStatus status) {
		log.info("Mocking EDS client");

		if (status.is2xxSuccessful()) {
			EdsResponseDTO response = new EdsResponseDTO();
			response.setEsito(status.is2xxSuccessful());
			response.setErrorMessage(status.is2xxSuccessful() ? null : "Test error");
			Mockito.doReturn(new ResponseEntity<>(response, status)).when(restTemplate)
					.exchange(anyString(), eq(HttpMethod.DELETE), eq(null), ArgumentMatchers.eq(EdsResponseDTO.class));
		} else if (status.is4xxClientError()) {
			Mockito.doThrow(new RestClientException("")).when(restTemplate)
					.exchange(anyString(), eq(HttpMethod.DELETE), eq(null), ArgumentMatchers.eq(EdsResponseDTO.class));
		} else {
			Mockito.doThrow(new BusinessException("")).when(restTemplate)
					.exchange(anyString(), eq(HttpMethod.DELETE), eq(null), ArgumentMatchers.eq(EdsResponseDTO.class));
		}
	}

	ResponseEntity<ResponseDTO> callDelete(final String documentId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(Constants.Headers.JWT_HEADER, generateJwt(null, false));

		final String urlReplace = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath() + "/v1/documents/" + documentId;

		HttpEntity<?> requestEntity = new HttpEntity<>(null, headers);
		return restTemplate.exchange(urlReplace, HttpMethod.DELETE, requestEntity, ResponseDTO.class);
	}

}
