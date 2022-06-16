package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import java.util.Date;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.ValidationCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.IValidationCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTHeaderDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ValidationCDAResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RawValidationEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.UIDModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ValidationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationErrorException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.logging.ElasticLoggerHelper;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.PDFUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;

/**
 * 
 * @author CPIERASC
 *
 *	Validation controller.
 */
@RestController
public class ValidationCTL extends AbstractCTL implements IValidationCTL {
	
	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 278537982196195315L;

	@Autowired
	private ValidationCFG validationCFG;

	@Autowired
	private IKafkaSRV kafkaSRV;

	@Autowired
	private ElasticLoggerHelper elasticLogger;
	
	@Override
	public ResponseEntity<ValidationCDAResDTO> validationCDA(ValidationCDAReqDTO requestBody, MultipartFile file, HttpServletRequest request) {

		final String transactionId = StringUtility.generateTransactionUID(UIDModeEnum.get(validationCFG.getTransactionIDStrategy()));

		Date startDateOperation = new Date();
		
		ValidationResultEnum result = null;
		String msgResult = null;
		
		final JWTTokenDTO jwtToken = extractJWT(request.getHeader(Constants.Headers.JWT_HEADER));

		ValidationCDAReqDTO jsonObj = getValidationJSONObject(request.getParameter("requestBody"));
		if (jsonObj==null) {

			try {
				ObjectMapper mapper = new ObjectMapper();
				mapper.readValue(request.getParameter("requestBody"), ValidationCDAReqDTO.class);
			} catch (UnrecognizedPropertyException ue) {
				msgResult = "Uno o più parametri non riconosciuti presenti all'interno della request";
			} catch (Exception e) {
				msgResult = "Errore generico nella letture della request.";
			}
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
			
			if (StringUtility.isNullOrEmpty(msgResult)) { 
				msgResult = checkValidationMandatoryElements(jsonObj);
				if (!StringUtility.isNullOrEmpty(msgResult)) {
					result = ValidationResultEnum.MANDATORY_ELEMENT_ERROR;
				} else {
					msgResult = checkFormatDate(jsonObj.getDataInizioPrestazione(), jsonObj.getDataFinePrestazione());
					if(!StringUtility.isNullOrEmpty(msgResult)) {
						result = ValidationResultEnum.FORMAT_ELEMENT_ERROR;
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
								if (StringUtility.isNullOrEmpty(cda)) {
									result = ValidationResultEnum.MINING_CDA_ERROR;
									msgResult = "Errore generico in fase di estrazione del CDA dal file.";
								} else {
									msgResult = validateJWT(jwtToken, cda);
									if (StringUtils.isEmpty(msgResult)) {
										ValidationInfoDTO validationRes = validate(cda, jsonObj.getActivity(), transactionId);
										if(validationRes!=null && !RawValidationEnum.OK.equals(validationRes.getResult())) {
											msgResult = validationRes.getMessage()!=null ? validationRes.getMessage().stream().collect(Collectors.joining(",")) : null;
											if (RawValidationEnum.SYNTAX_ERROR.equals(validationRes.getResult())) {
												result = ValidationResultEnum.SYNTAX_ERROR;
											} else if (RawValidationEnum.SEMANTIC_ERROR.equals(validationRes.getResult())) {
												result = ValidationResultEnum.SEMANTIC_ERROR;
											} else if(RawValidationEnum.VOCABULARY_ERROR.equals(validationRes.getResult())) {
												result = ValidationResultEnum.VOCABULARY_ERROR;
											} else {
												result = ValidationResultEnum.GENERIC_ERROR;
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
		}

		if(StringUtility.isNullOrEmpty(msgResult)) {
			kafkaSRV.sendValidationStatus(transactionId, EventStatusEnum.SUCCESS, null, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
        } else {
			kafkaSRV.sendValidationStatus(transactionId, EventStatusEnum.ERROR, msgResult, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
        }

		if (!ValidationResultEnum.OK.equals(result)) {
			elasticLogger.error(msgResult + " " + transactionId, OperationLogEnum.VAL_CDA2, ResultLogEnum.KO, startDateOperation, result != null ? result.getErrorCategory() : null);
			throw new ValidationErrorException(result, msgResult, transactionId);
		}

		elasticLogger.info("Validation CDA completed for transactionID " + transactionId, OperationLogEnum.VAL_CDA2, ResultLogEnum.OK, startDateOperation);

		if (jsonObj!=null && ActivityEnum.PRE_PUBLISHING.equals(jsonObj.getActivity())){
			return new ResponseEntity<>(new ValidationCDAResDTO(getLogTraceInfo(), transactionId), HttpStatus.CREATED);
		} 
		return new ResponseEntity<>(new ValidationCDAResDTO(getLogTraceInfo(), transactionId), HttpStatus.OK);
		
	}

}
