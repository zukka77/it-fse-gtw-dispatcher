package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTHeaderDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ValidationCDAResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthDataFormatEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PracticeSettingCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.TipoDocAltoLivEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ValidationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTest {


	@Autowired
	private RestTemplate restTemplate;

	@Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;
	
	public Map<String, ValidationResultEnum> callValidationWithoutToken(ActivityEnum activity, HealthDataFormatEnum type, InjectionModeEnum mode, byte[] fileByte){
		return callValidation(activity, type, mode, fileByte, false,null);
	}

	public Map<String, ValidationResultEnum> callValidation(ActivityEnum activity, HealthDataFormatEnum type, InjectionModeEnum mode, byte[] fileByte,
			boolean tokenPresent){
		return callValidation(activity, type, mode, fileByte,tokenPresent,null);
	}

	public Map<String, ValidationResultEnum> callValidation(ActivityEnum activity, HealthDataFormatEnum type, InjectionModeEnum mode, byte[] fileByte,
			boolean tokenPresent, ValidationCDAReqDTO reqDTO) {
		Map<String, ValidationResultEnum> output = new HashMap<>();
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
				map.add("requestBody", buildValidationReqDTO(activity, type, mode));
			} else {
				map.add("requestBody", reqDTO);
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			if(tokenPresent) {

				log.info("Simulating a valid json payload");
				headers.set(Constants.Headers.JWT_HEADER, generateJwt(StringUtility.encodeSHA256(fileByte)));
			}

			String urlValidation = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath() + "/v1.0.0/validate-creation";

			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

			ResponseEntity<ValidationCDAResDTO> response = restTemplate.exchange(urlValidation, HttpMethod.POST, requestEntity, ValidationCDAResDTO.class);
			if(ActivityEnum.VALIDATION.equals(activity)) {
				assertEquals(response.getStatusCode().value(), 200);
			} else if(ActivityEnum.PRE_PUBLISHING.equals(activity)) {
				assertEquals(response.getStatusCode().value(), 201);
			}

			output.put(response.getBody().getTransactionId(), ValidationResultEnum.OK);

		} catch (Exception ex) {
			String message = ex.getMessage();
			Integer firstIndex = message.indexOf("{");
			Integer lastIndex = message.indexOf("}");
			String subString = message.substring(firstIndex, lastIndex+1);

			ErrorResponseDTO errorClass = StringUtility.fromJSON(subString, ErrorResponseDTO.class);
			output.put("ERROR", ValidationResultEnum.get(errorClass.getType()));
			log.error("Error : " + ex.getMessage());
		}
		return output;
	}
	
	protected ValidationCDAReqDTO buildValidationReqDTO(ActivityEnum activity, HealthDataFormatEnum type, InjectionModeEnum mode) {
		ValidationCDAReqDTO validationReq = ValidationCDAReqDTO.builder().
				activity(activity).
				tipoDocumentoLivAlto(TipoDocAltoLivEnum.REFERTO).
				mode(mode).healthDataFormat(type).
				assettoOrganizzativo(PracticeSettingCodeEnum.AD_PSC001).
				identificativoPaziente(randomFiscalCode()).
				tipoAttivitaClinica(AttivitaClinicaEnum.CONSULTO).
				identificativoSottomissione("Identificativo sottomissione").
				build(); 
		
		return validationReq;
	}
	
	protected ValidationCDAReqDTO buildValidationReqDTOCustom(ActivityEnum activity, HealthDataFormatEnum type, InjectionModeEnum mode,
			TipoDocAltoLivEnum tipoDocLivelloAlto,PracticeSettingCodeEnum assettoOrganizzativo, 
			String identificativoPaziente, AttivitaClinicaEnum tipoAttivitaClinica, 
			String identificativoSottomissione) {
		ValidationCDAReqDTO validationReq = ValidationCDAReqDTO.builder().
				activity(activity).
				tipoDocumentoLivAlto(tipoDocLivelloAlto).
				mode(mode).healthDataFormat(type).
				assettoOrganizzativo(assettoOrganizzativo).
				identificativoPaziente(identificativoPaziente).
				tipoAttivitaClinica(tipoAttivitaClinica).
				identificativoSottomissione(identificativoSottomissione).
				build(); 
		
		return validationReq;
	}

	protected String randomFiscalCode() {
		// To generate a random fiscal code that passes CfUtility.isValid() requires too much effort.
		return "RSSMRA22A01A399Z";
	}

	protected String generateJwt(final String documentHash) {
		final JWTPayloadDTO jwtPayload = new JWTPayloadDTO("201123456", 1540890704L, 1540918800L, "1540918800", 
			Constants.App.JWT_TOKEN_AUDIENCE, "RSSMRA22A01A399Z", "Regione Lazio", "201", 
			"AAS", "RSSMRA22A01A399Z", true, "TREATMENT", null, "CREATE", documentHash);
		
		final JWTHeaderDTO jwtHeader = new JWTHeaderDTO("RS256", Constants.App.JWT_TOKEN_TYPE, null, "X5C cert base 64");

		StringBuilder encodedJwtToken = new StringBuilder(Constants.App.BEARER_PREFIX) // Bearer prefix
			.append(Base64.getEncoder().encodeToString(new Gson().toJson(jwtHeader).getBytes())) // Header
			.append(".").append(Base64.getEncoder().encodeToString(new Gson().toJson(jwtPayload).getBytes())); // Payload
		return encodedJwtToken.toString();
	}

}
