/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.exceptions.RecordNotFoundException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.exceptions.ServerResponseException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.IniClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.IniMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationMetadataReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniTraceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.INIErrorEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
class IniClientTest {

    @SpyBean
    RestTemplate restTemplate;

    @Autowired
    private IniClient iniClient;

    private IniMetadataUpdateReqDTO requestBody;

    @BeforeEach
    void init() {
    	//TODO
//        requestBody = new IniMetadataUpdateReqDTO("idDoc", new JWTPayloadDTO(), new PublicationMetadataReqDTO());
    }

    @Test
    @DisplayName("Update - updateConnectionRefusedErrorTest")
    void updateConnectionRefusedErrorTest() {
        Mockito.doThrow(new ConnectionRefusedException("url", "Error: connection refused")).when(restTemplate)
                .exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(IniTraceResponseDTO.class));
        assertThrows(ConnectionRefusedException.class, () -> iniClient.updateMetadati(requestBody));
    }

    @Test
    @DisplayName("Update - updateRecordNotFoundErrorTest")
    void updateRecordNotFoundErrorTest() {
        IniTraceResponseDTO responseMock = new IniTraceResponseDTO();
        responseMock.setEsito(false);
        responseMock.setErrorMessage(INIErrorEnum.RECORD_NOT_FOUND.name());
        Mockito.doReturn(new ResponseEntity<>(responseMock, HttpStatus.OK)).when(restTemplate)
                .exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(IniTraceResponseDTO.class));
        assertThrows(RecordNotFoundException.class, () -> iniClient.updateMetadati(requestBody));
    }

    @Test
    @DisplayName("Update - updateRecordGenericErrorTest")
    void updateRecordGenericErrorTest() {
        IniTraceResponseDTO responseMock = new IniTraceResponseDTO();
        responseMock.setEsito(false);
        Mockito.doReturn(new ResponseEntity<>(responseMock, HttpStatus.OK)).when(restTemplate)
                .exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(IniTraceResponseDTO.class));
        assertThrows(BusinessException.class, () -> iniClient.updateMetadati(requestBody));
    }

    @Test
    @DisplayName("Update - updateRecordGenericExceptionTest")
    void updateRecordGenericExceptionTest() {
        IniTraceResponseDTO responseMock = new IniTraceResponseDTO();
        responseMock.setEsito(false);
        Mockito.doThrow(new BusinessException("Error")).when(restTemplate)
                .exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(IniTraceResponseDTO.class));
        assertThrows(BusinessException.class, () -> iniClient.updateMetadati(requestBody));
    }

    @Test
    @DisplayName("Update - updateHttpStatucCodeExceptionTest")
    void updateHttpStatusCodeExceptionTest() {
        IniTraceResponseDTO responseMock = new IniTraceResponseDTO();
        responseMock.setEsito(false);
        Mockito.doThrow(new HttpStatusCodeException(HttpStatus.MULTI_STATUS) {
                    @Override
                    public HttpStatus getStatusCode() {
                        return super.getStatusCode();
                    }
                }).when(restTemplate)
                .exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(IniTraceResponseDTO.class));
        assertThrows(ServerResponseException.class, () -> iniClient.updateMetadati(requestBody));
    }

    @Test
    @DisplayName("Update - updateRecord Success test")
    void updateRecordSuccessTest() {
        IniTraceResponseDTO responseMock = new IniTraceResponseDTO();
        responseMock.setEsito(true);
        Mockito.doReturn(new ResponseEntity<>(responseMock, HttpStatus.OK)).when(restTemplate)
                .exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(IniTraceResponseDTO.class));
        IniTraceResponseDTO responseDTO = iniClient.updateMetadati(requestBody);
        assertEquals(responseDTO, responseMock);
    }

    @Test
    @DisplayName("Update - updateRecordHttpErrorTest")
    void updateRecordHttpErrorTest() {
        IniTraceResponseDTO responseMock = new IniTraceResponseDTO();
        responseMock.setEsito(true);
        Mockito.doReturn(new ResponseEntity<>(responseMock, HttpStatus.BAD_GATEWAY)).when(restTemplate)
                .exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(IniTraceResponseDTO.class));
        assertNull(iniClient.updateMetadati(requestBody).getErrorMessage());
    }

    @Test
    @DisplayName("Update - updateRecordHttpBodyNullTest")
    void updateRecordHttpBodyNullTest() {
        Mockito.doReturn(new ResponseEntity<>(null, HttpStatus.BAD_GATEWAY)).when(restTemplate)
                .exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(IniTraceResponseDTO.class));
        assertNull(iniClient.updateMetadati(requestBody));
    }
}
