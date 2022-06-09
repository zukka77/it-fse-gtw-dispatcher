package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.IHistoricalDocValidationCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTHeaderDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.HistoricalValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.HistoricalValidationCDAResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RawValidationEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ValidationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationErrorException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.PDFUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;

/**
 *	Historical Document Validation controller.
 */
@RestController
public class HistoricalValidationCTL extends AbstractCTL implements IHistoricalDocValidationCTL {
	
	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 278537982190095315L;

	@Autowired
	private IKafkaSRV kafkaSRV;
	
	@Override
	public HistoricalValidationCDAResDTO historicalValidationCDA(HistoricalValidationCDAReqDTO requestBody, MultipartFile file, HttpServletRequest request) {

		String workflowInstanceId = "";
		ValidationResultEnum result = null;
		String msgResult = null;

		final JWTTokenDTO jwtToken = extractJWT(request.getHeader(Constants.Headers.JWT_HEADER));

		HistoricalValidationCDAReqDTO jsonObj = getHistoricalValidationJSONObject(request.getParameter("requestBody"));
		if (jsonObj==null) {
			msgResult = "I parametri json devono essere valorizzati.";
			result = ValidationResultEnum.MANDATORY_ELEMENT_ERROR;
		} else {
			if (jwtToken == null) {
				msgResult = "Il JWT deve essere valorizzato.";
				result = ValidationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN;
			} else {
				msgResult = JWTHeaderDTO.validateHeader(jwtToken.getHeader());
				if (msgResult == null) {
					msgResult = JWTPayloadDTO.validatePayload(jwtToken.getPayload());
				}
				if (msgResult != null) {
					result = ValidationResultEnum.INVALID_TOKEN_FIELD;
				}
			}
			if (jwtToken == null) {
				result = ValidationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN;
			} else {
				msgResult = checkHistoricalValidationMandatoryElements(jsonObj);
				if (!StringUtility.isNullOrEmpty(msgResult)) {
					result = ValidationResultEnum.MANDATORY_ELEMENT_ERROR;
				} else {
					byte[] bytes = checkFile(file);
					if (bytes == null) {
						msgResult = "Il file deve essere valorizzato";
						result = ValidationResultEnum.EMPTY_FILE_ERROR;
					} else {
						if (!PDFUtility.isPdf(bytes)) {
							result = ValidationResultEnum.DOCUMENT_TYPE_ERROR;
							msgResult = "Il file deve essere un PDF.";
						} else {
							String cda = extractCDA(bytes, jsonObj.getMode());
							
							try {
								workflowInstanceId = CdaUtility.getWorkflowInstanceId(cda);
							} catch(Exception ex) {
								msgResult = "Errore durante l'estrazione del workflow instance id ";
								result = ValidationResultEnum.WORKFLOW_ID_ERROR;
							}
							
							if (StringUtility.isNullOrEmpty(cda)) {
								result = ValidationResultEnum.MINING_CDA_ERROR;
								msgResult = "Errore generico in fase di estrazione del CDA dal file.";
							} else {

								msgResult = validateJWT(jwtToken, cda);
								if (StringUtils.isEmpty(msgResult)) {
									ValidationInfoDTO validationRes = validate(cda, jsonObj.getActivity(), workflowInstanceId);
									if(validationRes!=null && !RawValidationEnum.OK.equals(validationRes.getResult())) {
										msgResult = validationRes.getMessage().get(0);
										if (RawValidationEnum.SYNTAX_ERROR.equals(validationRes.getResult())) {
											result = ValidationResultEnum.SYNTAX_ERROR;
										} else if (RawValidationEnum.SEMANTIC_ERROR.equals(validationRes.getResult())) {
											result = ValidationResultEnum.SEMANTIC_ERROR;
										} else if(RawValidationEnum.VOCABULARY_ERROR.equals(validationRes.getResult())) {
											result = ValidationResultEnum.VOCABULARY_ERROR;
										}  
									} else {
										result = ValidationResultEnum.OK;
									} 
								}
							}
						}
					}
				}
			}
		}

		if(StringUtility.isNullOrEmpty(msgResult)){
			kafkaSRV.sendHistoricalValidationStatus(workflowInstanceId, EventStatusEnum.SUCCESS, null, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
        } else {
			kafkaSRV.sendHistoricalValidationStatus(workflowInstanceId, EventStatusEnum.ERROR, msgResult, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
        }

		if (!ValidationResultEnum.OK.equals(result)) {
			throw new ValidationErrorException(result, msgResult, workflowInstanceId);
		}

		return new HistoricalValidationCDAResDTO(getLogTraceInfo(), workflowInstanceId);
	}
 
}
