package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.exceptions.ServerResponseException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.EdsClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.IniClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.DeleteRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.EdsTraceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniTraceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
class DeleteTest extends AbstractTest {

	@Autowired
	ServletWebServerApplicationContext webServerAppCtxt;

	@Autowired
	RestTemplate restTemplate;

	@MockBean
	EdsClient edsClient;

	@MockBean
	IniClient iniClient;

	@Test
	@DisplayName("Delete of a document")
	void givenAnIdentifier_shouldExecuteDeletion() {

		final String idDocument = StringUtility.generateUUID();
		mockEdsClient(HttpStatus.OK);
		mockIniClient(HttpStatus.OK);

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

		mockIniClient(HttpStatus.INTERNAL_SERVER_ERROR);
		mockEdsClient(HttpStatus.OK);
		assertThrows(HttpServerErrorException.InternalServerError.class, () -> callDelete(idDocument));
	}

	@Test
	@DisplayName("EDS Error returned")
	void whenEdsFails_anErrorShouldBeReturned() {

		final String idDocument = StringUtility.generateUUID();

		mockIniClient(HttpStatus.OK);
		mockEdsClient(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThrows(HttpServerErrorException.InternalServerError.class, () -> callDelete(idDocument));
	}

	void mockIniClient(final HttpStatus status) {
		log.debug("Mocking INI client");

		if (status.is2xxSuccessful()) {
			IniTraceResponseDTO response = new IniTraceResponseDTO();
			response.setSpanID(StringUtility.generateUUID());
			response.setTraceID(StringUtility.generateUUID());
			response.setEsito(true);
			response.setErrorMessage(null);
			given(iniClient.delete(any(DeleteRequestDTO.class))).willReturn(response);
		} else if (status.is4xxClientError()) {
			given(iniClient.delete(any(DeleteRequestDTO.class))).willThrow(new HttpClientErrorException(status));
		} else {
			given(iniClient.delete(any(DeleteRequestDTO.class))).willThrow(new ServerResponseException("Test error"));
		}

	}


	void mockEdsClient(final HttpStatus status) {
		log.debug("Mocking EDS client");

		if (status.is2xxSuccessful()) {
			EdsTraceResponseDTO response = new EdsTraceResponseDTO();
			response.setSpanID(StringUtility.generateUUID());
			response.setTraceID(StringUtility.generateUUID());
			response.setEsito(status.is2xxSuccessful() ? true : false);
			response.setErrorMessage(status.is2xxSuccessful() ? null : "Test error");
			given(edsClient.delete(anyString())).willReturn(response);
		} else if (status.is4xxClientError()) {
			given(edsClient.delete(anyString())).willThrow(new HttpClientErrorException(status));
		} else {
			given(edsClient.delete(anyString())).willThrow(new ServerResponseException("Test error"));
		}

	}

	ResponseEntity<ResponseDTO> callDelete(final String documentId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(Constants.Headers.JWT_HEADER, generateJwt(null, false));

		final String urlReplace = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath() + "/v1/documents/" + documentId;

		Map<String, String> param = new HashMap<>();
		param.put("identificativoDocUpdate", documentId);

		HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(param, headers);
		return restTemplate.exchange(urlReplace, HttpMethod.DELETE, requestEntity, ResponseDTO.class);
	}

}
