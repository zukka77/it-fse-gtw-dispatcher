package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IValidatorClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.CDACFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.ValidationCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.IHistoricalDocValidationCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.AttachmentDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ValidationCDAResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RawValidationEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.UIDModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ValidationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationErrorException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.facade.ICdaFacadeSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.PDFUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 *
 *	Historical Document Validation controller.
 */
@RestController
@Slf4j
public class HistoricalValidationCTL extends AbstractCTL implements IHistoricalDocValidationCTL {
	
	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 278537982190095315L;

	@Autowired
	private ICdaFacadeSRV cdaFacadeSRV;
	
	@Autowired
	private CDACFG cdaCfg;

	@Autowired
	private ValidationCFG validationCFG;

	@Autowired
	private IValidatorClient validatorClient;

	@Autowired
	private IKafkaSRV kafkaSRV;
	
	@Override
	public ValidationCDAResDTO historicalValidationCDA(ValidationCDAReqDTO requestBody, MultipartFile file, HttpServletRequest request) {

		final String transactionId = StringUtility.generateTransactionUID(UIDModeEnum.get(validationCFG.getTransactionIDStrategy()));
		ValidationResultEnum result = null;
		String msgResult = null;

		String jwt = request.getHeader("Authorization");
		ValidationCDAReqDTO jsonObj = getJSONObject(request.getParameter("requestBody"));
		if (jsonObj==null) {
			msgResult = "I parametri json devono essere valorizzati.";
			result = ValidationResultEnum.MANDATORY_ELEMENT_ERROR;
		} else {
			msgResult = checkTokenMandatory(jwt);
			if (!StringUtility.isNullOrEmpty(msgResult)) {
				result = ValidationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN;
			} else {
				msgResult = checkMandatoryElements(jsonObj);
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
							if (StringUtility.isNullOrEmpty(cda)) {
								result = ValidationResultEnum.MINING_CDA_ERROR;
								msgResult = "Errore generico in fase di estrazione del CDA dal file.";
							} else {
								ValidationInfoDTO validationRes = validate(cda, jsonObj.getActivity(), transactionId);
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

		if(jsonObj!=null && (!validationCFG.getSaveValidationErrorOnly() || (validationCFG.getSaveValidationErrorOnly() && !ValidationResultEnum.OK.equals(result)))) {
			try {
				kafkaSRV.notifyValidationEvent(jsonObj, result, true, false, transactionId);
			} catch (Exception e) {
				log.warn("Error sending Kafka notification for CDA validation with TxID: " + transactionId);
			}
		}

		if (!ValidationResultEnum.OK.equals(result)) {
			throw new ValidationErrorException(result, msgResult);
		}

		return new ValidationCDAResDTO(getLogTraceInfo(), transactionId);
	}

	private String checkMandatoryElements(ValidationCDAReqDTO jsonObj) {
		String out = null;
		
			if (jsonObj.getActivity()==null) {
				out = "Il campo activity deve essere valorizzato.";
			} else if (jsonObj.getTipoDocumentoLivAlto()==null) {
				out = "Il tipo documento di alto livello deve essere valorizzato.";
			} else if (jsonObj.getAssettoOrganizzativo()==null) {
				out = "L'assetto organizzativo deve essere valorizzato.";
			} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoPaziente())) {
				out = "L'identificativo paziente deve essere valorizzato.";
			} else if (jsonObj.getTipoAttivitaClinica()==null) {
				out = "Il tipo attività clinica deve essere valorizzata.";
			} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoSottomissione())) {
				out = "L'identificativo della sottomissione deve essere valorizzato.";
			}
		return out;
	}
	
	private String checkTokenMandatory(String jwt) {
    	String out = null;
    	if (StringUtility.isNullOrEmpty(jwt)) {
    		out = "Il JWT deve essere valorizzato.";
    	}
    	return out;
    }
	
	private byte[] checkFile(MultipartFile file) {
		byte[] out = null;
		try {
			if (file!=null&&file.getBytes()!=null && file.getBytes().length>0) {
				out = file.getBytes(); 
			}
		} catch (IOException e1) {
			log.error("Generic error io in cda :" , e1);
		}
		return out;
	}
	
	private String extractCDA(byte[] bytesPDF, InjectionModeEnum mode) {
		String out = null;
		if (InjectionModeEnum.RESOURCE.equals(mode)) {
			out = PDFUtility.unenvelopeA2(bytesPDF);
		} else if (InjectionModeEnum.ATTACHMENT.equals(mode)) {
			out = extractCDAFromAttachments(bytesPDF);
		} else {
			out = PDFUtility.unenvelopeA2(bytesPDF);
			if (StringUtility.isNullOrEmpty(out)) {
				out = extractCDAFromAttachments(bytesPDF);
			}
		}
		return out;
	}

	private ValidationCDAReqDTO getJSONObject(String jsonREQ) {
		ValidationCDAReqDTO out = null;
		if(!StringUtility.isNullOrEmpty(jsonREQ)) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				out = mapper.readValue(jsonREQ, ValidationCDAReqDTO.class);
			} catch (Exception ex) {
				log.error("Errore gestione json :" , ex);
			}
		}
		return out;
	}


	private ValidationInfoDTO validate(String cda, ActivityEnum activity, String transactionID) {
		ValidationInfoDTO rawValidationRes = null;
		try {
			rawValidationRes = validatorClient.validate(cda);
			if(rawValidationRes!=null) {
				RawValidationEnum rawValidation = rawValidationRes.getResult();
				if (RawValidationEnum.OK.equals(rawValidation)) {
					if (ActivityEnum.HISTORICAL_DOC_PRE_PUBLISHING.equals(activity)) {
						String hashedCDA = StringUtility.encodeSHA256B64(cda);
						cdaFacadeSRV.create(transactionID, hashedCDA);
					}
				}  
			}
		}  catch(ConnectionRefusedException cex) {
			throw cex;
		} catch(Exception ex) {
			log.error("Error while validate : " , ex);
			throw new BusinessException("Error while validate : " , ex);
		}
		return rawValidationRes;
	}

	private String extractCDAFromAttachments(byte[] cda) {
		String out = null;
		Map<String, AttachmentDTO> attachments = PDFUtility.extractAttachments(cda);
		if (attachments!= null && attachments.size()>0) {
			if (attachments!= null && attachments.size() == 1) {
				out = new String(attachments.values().iterator().next().getContent());
			} else {
				AttachmentDTO attDTO = attachments.get(cdaCfg.getCdaAttachmentName());
				if (attDTO != null) {
					out = new String(attDTO.getContent());
				}
			}
		}
		return out;
	}
 
}