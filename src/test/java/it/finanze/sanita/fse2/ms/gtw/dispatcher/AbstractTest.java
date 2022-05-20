package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

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
			boolean tokenPresent,ValidationCDAReqDTO reqDTO) {
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
				headers.set("Authorization", "test");
			}

			String urlValidation = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath() + "/v1/validate-creation";

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
				identificativoPaziente("VI").
				tipoAttivitaClinica(AttivitaClinicaEnum.CONSULTO).
				identificativoSottomissione("Identifiativo sottomissione").
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
}


