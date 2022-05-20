package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IValidatorClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.CDACFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.ValidationCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.ITSFeedingCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.AttachmentDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.PublicationOutputDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.SignatureValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.PublicationCreationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RawValidationEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.SignVerificationModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationPublicationErrorException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.signer.SignerHelper;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.PDFUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 *	Historical document controller.
 */
@RestController
@Slf4j
public class TSFeedingCTL extends AbstractCTL implements ITSFeedingCTL {

    @Value("${sign.verification.mode}")
    private String signVerificationMode;
    
    @Autowired
    private CDACFG cdaCfg;

    @Autowired
    private ValidationCFG validationCFG;
    
    @Autowired
    private IKafkaSRV kafkaSRV;

    @Autowired
	private IValidatorClient validatorClient;
    

    @Override
    public PublicationCreationResDTO tsDocProcessing(PublicationCreationReqDTO requestBody, MultipartFile file, HttpServletRequest request) {

        String jwt = request.getHeader("Authorization");
        PublicationCreationReqDTO jsonObj = getJSONObject(request.getParameter("requestBody"));
        byte[] bytePDF = null;
        PublicationOutputDTO out = null;

        if (jsonObj==null) {
        	out = PublicationOutputDTO.builder().msg("I parametri json devono essere valorizzati.").result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR).build();
        } else {
        	if (!jsonObj.isForcePublish()) {
        		
        		String mandatoryTokenMessage = checkTokenMandatory(jwt);
        		if (!StringUtility.isNullOrEmpty(mandatoryTokenMessage)) {
                	out = PublicationOutputDTO.builder().msg(mandatoryTokenMessage).result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN).build();
                }
        		if(out == null) {
        			String msgMandatoryElement = checkMandatoryElements(jsonObj);
        			if (!StringUtility.isNullOrEmpty(msgMandatoryElement)) {
        				out = PublicationOutputDTO.builder().msg(msgMandatoryElement).result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR).build();
        			}
        			
        			if (out == null) {
        				bytePDF = checkFile(file);
        				if (bytePDF == null) {
        					out = PublicationOutputDTO.builder().msg("Il file deve essere valorizzato").result(PublicationResultEnum.EMPTY_FILE_ERROR).build();
        				} else {
        					if (!PDFUtility.isPdf(bytePDF)) {
                                out = PublicationOutputDTO.builder().msg("Il file deve essere un PDF")
                                        .result(PublicationResultEnum.DOCUMENT_TYPE_ERROR).build();
                            } else {

                                String cda = extractCDA(bytePDF, jsonObj.getMode());
                                if (StringUtility.isNullOrEmpty(cda)) {
                                    out = PublicationOutputDTO.builder().msg("Errore generico in fase di estrazione del CDA dal file.")
                                        .result(PublicationResultEnum.MINING_CDA_ERROR).build();
                                } else {
                                    ValidationInfoDTO validationRes = validate(cda,jsonObj.getTransactionID());
                                    if (!RawValidationEnum.OK.equals(validationRes.getResult())) {
                                        if (RawValidationEnum.SYNTAX_ERROR.equals(validationRes.getResult())) {
                                            out = PublicationOutputDTO.builder().msg("Errore sintattico.")
                                                    .result(PublicationResultEnum.SYNTAX_ERROR).build();
                                        } else if (RawValidationEnum.SEMANTIC_ERROR.equals(validationRes.getResult())) {
                                            out = PublicationOutputDTO.builder().msg("Errore semantico.")
                                                    .result(PublicationResultEnum.SEMANTIC_ERROR).build();
                                        } else if (RawValidationEnum.VOCABULARY_ERROR
                                                .equals(validationRes.getResult())) {
                                            out = PublicationOutputDTO.builder().msg("Errore vocabolario.")
                                                    .result(PublicationResultEnum.VOCABULARY_ERROR).build();
                                        }
                                    }
                                }

                                out = handleCDA(jsonObj, bytePDF);
                        	}
        				}
        			}
        			
        			if(out == null) {
        				SignVerificationModeEnum signVM = SignVerificationModeEnum.get(signVerificationMode);
        				SignatureValidationDTO signatureValidation = validateSignature(bytePDF, signVM);
        				if(Boolean.FALSE.equals(signatureValidation.getStatus())) {
        					out = PublicationOutputDTO.builder().msg("Validazione firma fallita").result(PublicationResultEnum.SIGNED_VALIDATION_ERROR).build();
        				}
        			}
        		}
        	}
        }

        if(!validationCFG.getSaveValidationErrorOnly() || (validationCFG.getSaveValidationErrorOnly() && out!=null)){
            try {
            	PublicationResultEnum result = null;
            	if (out!=null) {
            		result = out.getResult();
            	} else {
            		result = PublicationResultEnum.OK;
            		if (jsonObj!=null && jsonObj.isForcePublish()) {
                		result = PublicationResultEnum.OK_FORCED;
            		}
            	}
                kafkaSRV.notifyPublicationEvent(jsonObj, result, false, true);
            } catch (Exception e) {
            	String transactionIdError = jsonObj!=null ? jsonObj.getTransactionID() : null;
                log.warn("Error sending Kafka notification for publication event with TxID: " + transactionIdError);
            }
        }
        
        if (out!=null) {
            throw new ValidationPublicationErrorException(out.getResult(), out.getMsg());
        }
        return new PublicationCreationResDTO(getLogTraceInfo());
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

	private PublicationOutputDTO handleCDA(PublicationCreationReqDTO jsonObj, byte[] bytePDF) {
		PublicationOutputDTO out = null;
		//CDA mining.
		String cda = null;
		if (InjectionModeEnum.RESOURCE.equals(jsonObj.getMode())) {
		    cda = PDFUtility.unenvelopeA2(bytePDF);
		} else if (InjectionModeEnum.ATTACHMENT.equals(jsonObj.getMode())) {
		    cda = extractCDAFromAttachments(bytePDF);
		} else {
		    cda = PDFUtility.unenvelopeA2(bytePDF);
		    if (StringUtility.isNullOrEmpty(cda)) {
		        cda = extractCDAFromAttachments(bytePDF);
		    }
		}
        if (StringUtility.isNullOrEmpty(cda)){
            out = PublicationOutputDTO.builder().msg("Impossibile estrarre il CDA").result(PublicationResultEnum.MINING_CDA_ERROR).build();
        }
        return out;
	}

	private ValidationInfoDTO validate(String cda, String transactionID) {
		ValidationInfoDTO rawValidationRes = null;
		try {
			rawValidationRes = validatorClient.validate(cda);
		}  catch(ConnectionRefusedException cex) {
			throw cex;
		} catch(Exception ex) {
			log.error("Error while validate : " , ex);
			throw new BusinessException("Error while validate : " , ex);
		}
		return rawValidationRes;
	}
    
    private SignatureValidationDTO validateSignature(byte[] bytePDF, SignVerificationModeEnum signVM) {
        SignatureValidationDTO output = null;
        try {
            output = SignerHelper.validate(bytePDF, signVM);
        } catch (Exception ex) {
            log.error("Error while validate signature :" , ex);
            throw new BusinessException("Error while validate signature :" , ex);
        }
        return output;
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
    
    private String checkMandatoryElements(PublicationCreationReqDTO jsonObj) {
    	String out = null;
    	if (StringUtility.isNullOrEmpty(jsonObj.getTransactionID())) {
    		out = "Il campo txID deve essere valorizzato.";
    	} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoDoc())) {
    		out = "Il campo identificativo documento deve essere valorizzato.";
    	} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoRep())) {
    		out = "Il campo identificativo rep deve essere valorizzato.";
    	} else if (jsonObj.getTipoDocumentoLivAlto()==null) {
    		out = "Il campo tipo documento liv alto deve essere valorizzato.";
    	} else if (jsonObj.getAssettoOrganizzativo()==null) {
    		out = "Il campo assetto organizzativo deve essere valorizzato.";
    	} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoPaziente())) {
    		out = "Il campo identificativo paziente deve essere valorizzato.";
    	} else if (jsonObj.getTipoAttivitaClinica()==null) {
    		out = "Il campo tipo attivita clinica deve essere valorizzato.";
    	} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoSottomissione())) {
    		out = "Il campo identificativo sottomissione deve essere valorizzato.";
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
    
    private PublicationCreationReqDTO getJSONObject(String jsonREQ) {
        PublicationCreationReqDTO out = null;
        if(!StringUtility.isNullOrEmpty(jsonREQ)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                out = mapper.readValue(jsonREQ, PublicationCreationReqDTO.class);
            } catch (Exception ex) {
                log.error("Errore gestione json :" , ex);
            }
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

        
}

