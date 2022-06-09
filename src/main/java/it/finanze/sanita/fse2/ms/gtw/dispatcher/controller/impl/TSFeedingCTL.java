package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.ITSFeedingCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTHeaderDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.PublicationOutputDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.TSPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TSPublicationCreationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RawValidationEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationPublicationErrorException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.logging.ElasticLoggerHelper;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDocumentReferenceSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.facade.ICdaFacadeSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl.IniEdsInvocationSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.PDFUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 *	TS document feeding controller.
 */
@Slf4j
@RestController
public class TSFeedingCTL extends AbstractCTL implements ITSFeedingCTL {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -8596007149594204537L;
    
    @Autowired
    private IDocumentReferenceSRV documentReferenceSRV;

    @Autowired
    private IniEdsInvocationSRV iniEdsInvocationSRV;
    
    @Autowired
    private IKafkaSRV kafkaSRV;

    @Autowired
	private ElasticLoggerHelper elasticLogger;
    
    @Autowired
    private ICdaFacadeSRV cdaFacadeSRV;
    

    @Override
    public TSPublicationCreationResDTO tsFeeding(TSPublicationCreationReqDTO requestBody, MultipartFile file, HttpServletRequest request) {

        String workflowInstanceId = "";
		Date startDateOperation = new Date();
        final JWTTokenDTO jwtToken = extractJWT(request.getHeader(Constants.Headers.JWT_HEADER));

        TSPublicationCreationReqDTO jsonObj = getTSPublicationJSONObject(request.getParameter("requestBody"));

        byte[] bytePDF = null;
        PublicationOutputDTO out = null;
        String cda = "";

        if (jsonObj==null) {
        	out = PublicationOutputDTO.builder().msg(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()).result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR).build();
        } else {
                if (jwtToken == null) {
                    out = PublicationOutputDTO.builder().msg(PublicationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.getTitle()).result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN).build();
                } else {
                    String errorMsg = JWTHeaderDTO.validateHeader(jwtToken.getHeader());
                    if (errorMsg == null) {
                        errorMsg = JWTPayloadDTO.validatePayload(jwtToken.getPayload());
                    }
                    if (errorMsg != null) {
                        out = PublicationOutputDTO.builder().msg(errorMsg).result(PublicationResultEnum.INVALID_TOKEN_FIELD).build();
                    }
                }
        		if(out == null) {
        			String msgMandatoryElement = checkTSPublicationMandatoryElements(jsonObj);
        			if (!StringUtility.isNullOrEmpty(msgMandatoryElement)) {
        				out = PublicationOutputDTO.builder().msg(msgMandatoryElement).result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR).build();
        			} else {
    					String checkDateFormat = checkFormatDate(jsonObj.getDataInizioPrestazione(), jsonObj.getDataFinePrestazione());
    					if (!StringUtility.isNullOrEmpty(checkDateFormat)) {
    						out = PublicationOutputDTO.builder().msg(checkDateFormat).result(PublicationResultEnum.FORMAT_ELEMENT_ERROR).build();
    					}
    				}
        			
        			if (out == null) { 
        				bytePDF = checkFile(file);
        				if (bytePDF == null) {
        					out = PublicationOutputDTO.builder().msg(PublicationResultEnum.EMPTY_FILE_ERROR.getTitle()).result(PublicationResultEnum.EMPTY_FILE_ERROR).build();
        				} else {
        					if (!PDFUtility.isPdf(bytePDF)) {
                                out = PublicationOutputDTO.builder().msg(PublicationResultEnum.DOCUMENT_TYPE_ERROR.getTitle())
                                        .result(PublicationResultEnum.DOCUMENT_TYPE_ERROR).build();
                            } else {

                                cda = extractCDA(bytePDF, jsonObj.getMode());
                                if (StringUtility.isNullOrEmpty(cda)) {
                                    out = PublicationOutputDTO.builder().msg(PublicationResultEnum.MINING_CDA_ERROR.getTitle()).result(PublicationResultEnum.MINING_CDA_ERROR).build();
                                } else {
                                	try {
										workflowInstanceId = CdaUtility.getWorkflowInstanceId(cda);
									} catch(Exception ex) {
										 out = PublicationOutputDTO.builder().msg("Errore generico in fase di estrazione del workflow instance id.")
			                                        .result(PublicationResultEnum.WORKFLOW_ID_ERROR).build();
									}
                                	
                                	if(out==null) {
                                		if (!StringUtils.isEmpty(validateJWT(jwtToken, cda))) {
                                			out = PublicationOutputDTO.builder()
                                					.msg(PublicationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.getTitle())
                                					.result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN).build();
                                		} else if(!jsonObj.isForcePublish()) {
                                			ValidationInfoDTO validationRes = validate(cda, ActivityEnum.TS_PRE_PUBLISHING, workflowInstanceId);
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
                                	}
                                	
                                }
                        	}
        				}
        			}
        			
                    String documentSha256 = null;
                            
        			if(out == null) {
        				documentSha256 = StringUtility.encodeSHA256(bytePDF);
						out = validateDocumentHash(documentSha256, jwtToken);
        			}

                    if(out == null) {
        				Integer size = bytePDF!=null ? bytePDF.length : 0;
        				if(size==0) {
        					out = PublicationOutputDTO.builder().msg(PublicationResultEnum.DOCUMENT_SIZE_ERROR.getTitle()).result(PublicationResultEnum.DOCUMENT_SIZE_ERROR).build();
        				}
        				
        				if(out==null) {
        					
							PublicationCreationReqDTO publicationDTO = constructPublicationDTO(jsonObj);
        					FhirResourceDTO fhirResourcesDTO = documentReferenceSRV.createFhirResources(cda, publicationDTO, size, documentSha256);
        					if(fhirResourcesDTO!=null &&  !StringUtility.isNullOrEmpty(fhirResourcesDTO.getErrorMessage())) {
        						out = PublicationOutputDTO.builder().msg(fhirResourcesDTO.getErrorMessage()).result(PublicationResultEnum.FHIR_MAPPING_ERROR).build();
        					}
        					
        					if(out == null) {
        						Boolean isInserted = iniEdsInvocationSRV.insert(workflowInstanceId, fhirResourcesDTO, jwtToken);
        						if(Boolean.TRUE.equals(isInserted)) {
									// notify publisher skipping INI invocation
        							kafkaSRV.notifyPublisher(workflowInstanceId);
        						} else {
        							log.warn("Attention, insertion of transaction id and document reference not done on mongo");
        						}
        					}
        				}
        				
        			}
        		}
        }

        if(out==null){
        	if (jsonObj!=null && Boolean.FALSE.equals(jsonObj.isForcePublish())) {
				cdaFacadeSRV.consumeHash(workflowInstanceId);
			}
            kafkaSRV.sendFeedingStatus(workflowInstanceId, EventStatusEnum.SUCCESS, null, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
        } else {
            kafkaSRV.sendFeedingStatus(workflowInstanceId, EventStatusEnum.ERROR, out.getMsg(), jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
        }
        
       if (out!=null) {
           elasticLogger.error(out.getMsg() + " " + workflowInstanceId, OperationLogEnum.PUB_CDA2, ResultLogEnum.KO, startDateOperation, out.getResult().getErrorCategory());
           throw new ValidationPublicationErrorException(out.getResult(), out.getMsg());
       }
	   elasticLogger.info(String.format("Publication CDA received from TS completed for workflow instance id %s", workflowInstanceId), OperationLogEnum.PUB_CDA2, ResultLogEnum.OK, startDateOperation);
       return new TSPublicationCreationResDTO(getLogTraceInfo(), workflowInstanceId);
   }
        
}

