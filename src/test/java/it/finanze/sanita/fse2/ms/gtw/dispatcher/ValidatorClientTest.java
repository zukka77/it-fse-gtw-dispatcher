package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.ValidatorClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.ValidationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RawValidationEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles("TEST")
class ValidatorClientTest {

    @SpyBean
    RestTemplate restTemplate;

    @Autowired
    private ValidatorClient validatorClient;

    private static final String MOCK_CDA = "MOCK_CDA";

    @Test
    @DisplayName("validate - validateConnectionRefusedErrorTest")
    void validateConnectionRefusedErrorTest() {
        Mockito.doThrow(new RestClientException("url")).when(restTemplate)
                .exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(ValidationResDTO.class));
        assertThrows(ConnectionRefusedException.class, () -> validatorClient.validate(MOCK_CDA));
    }

    @Test
    @DisplayName("validate - validateRecordGenericExceptionTest")
    void validateRecordGenericExceptionTest() {
        Mockito.doThrow(new BusinessException("Error")).when(restTemplate)
                .exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(ValidationResDTO.class));
        assertThrows(BusinessException.class, () -> validatorClient.validate(MOCK_CDA));
    }

    @Test
    @DisplayName("validate - validateRecord Success test")
    void validateRecordSuccessTest() {
        LogTraceInfoDTO logTraceInfoDTO = new LogTraceInfoDTO("spanId", "traceId");
        ValidationInfoDTO validationInfoDTO = new ValidationInfoDTO();
        validationInfoDTO.setResult(RawValidationEnum.OK);
        ValidationResDTO responseMock = new ValidationResDTO(logTraceInfoDTO, validationInfoDTO);
        responseMock.getResult().setResult(RawValidationEnum.OK);
        Mockito.doReturn(new ResponseEntity<>(responseMock, HttpStatus.OK)).when(restTemplate)
                .exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(ValidationResDTO.class));
        ValidationInfoDTO responseDTO = validatorClient.validate(MOCK_CDA);
        assertEquals(responseDTO, responseMock.getResult());
    }
}
