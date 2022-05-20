package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.CDACFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.ValidationCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.IPublicationCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.AttachmentDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.PublicationOutputDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.SignatureValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.PublicationCreationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.SignVerificationModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationPublicationErrorException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDocumentReferenceSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.facade.ICdaFacadeSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl.IniEdsInvocationSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.signer.SignerHelper;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.PDFUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;
/**
 * 
 * @author CPIERASC
 *
 *  Publication controller.
 */
@RestController
@Slf4j
public class PublicationCTL extends AbstractCTL implements IPublicationCTL {
    /**
     * Serial version uid.
     */
    private static final long serialVersionUID = 1711126466058952723L;
    
    @Value("${sign.verification.mode}")
    private String signVerificationMode;
    
    @Autowired
    private ICdaFacadeSRV cdaFacadeSRV;
    
    @Autowired
    private CDACFG cdaCfg;
    
    @Autowired
    private ValidationCFG validationCFG;
    
    @Autowired
    private IKafkaSRV kafkaSRV;
    
    @Autowired
    private IDocumentReferenceSRV documentReferenceSRV;
    
    @Autowired
    private IniEdsInvocationSRV iniInvocationSRV;
    
    @Override
    public PublicationCreationResDTO publicationCreation(PublicationCreationReqDTO requestBody, MultipartFile file, HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        PublicationCreationReqDTO jsonObj = getJSONObject(request.getParameter("requestBody"));
        byte[] bytePDF = null;
        PublicationOutputDTO out = null;

        String cda = "";
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
        						out = PublicationOutputDTO.builder().msg("Il file deve essere un PDF").result(PublicationResultEnum.DOCUMENT_TYPE_ERROR).build();
        					} else {
        						cda = extractCDA(jsonObj, bytePDF);
        						out = handleCDA(cda,jsonObj.getTransactionID());
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
        			
        			if(out == null) {
        				String transactionID = jsonObj.getTransactionID();
        				Integer size = bytePDF!=null ? bytePDF.length : 0;
        				if(size==0) {
        					out = PublicationOutputDTO.builder().msg("Attenzione . Il file risulta essere vuoto").result(PublicationResultEnum.DOCUMENT_SIZE_ERROR).build();
        				}
        				
        				if(out==null) {
        					byte[] hash = StringUtility.encodeSHA256(bytePDF);
        					
        					FhirResourceDTO fhirResourcesDTO = documentReferenceSRV.createFhirResources(cda, jsonObj, size, hash);
        					if(fhirResourcesDTO!=null &&  !StringUtility.isNullOrEmpty(fhirResourcesDTO.getErrorMessage())) {
        						out = PublicationOutputDTO.builder().msg(fhirResourcesDTO.getErrorMessage()).result(PublicationResultEnum.FHIR_MAPPING_ERROR).build();
        					}
        					
        					if(out == null) {
        						Boolean isInserted = iniInvocationSRV.insert(transactionID, fhirResourcesDTO);
        						if(Boolean.TRUE.equals(isInserted)) {
        							kafkaSRV.notifyAfterSaveMapping(transactionID);
        						} else {
        							log.warn("Attention, insertion of transaction id and document reference not done on mongo");
        						}
        					}
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
                kafkaSRV.notifyPublicationEvent(jsonObj, result, false, false);
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

	private PublicationOutputDTO handleCDA(String cda , String transactionID) {
		PublicationOutputDTO out = null; 
        if (!StringUtility.isNullOrEmpty(cda)){
            final String hashedCDA = StringUtility.encodeSHA256B64(cda);
            final boolean matchingHash = cdaFacadeSRV.validateHash(hashedCDA, transactionID);
            if (!matchingHash) {
            	out = PublicationOutputDTO.builder().msg("Il CDA non risulta validato").result(PublicationResultEnum.CDA_MATCH_ERROR).build();
            }
        } else {
        	out = PublicationOutputDTO.builder().msg("Impossibile estrarre il CDA").result(PublicationResultEnum.MINING_CDA_ERROR).build();
        }
        return out;
	}
	
	private String extractCDA(PublicationCreationReqDTO jsonObj, byte[] bytePDF) {
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
		return cda;
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