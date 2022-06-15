// package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;
// import javax.servlet.http.HttpServletRequest;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.multipart.MultipartFile;

// import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.IHistoricalDocPublicationCTL;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTHeaderDTO;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.PublicationOutputDTO;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.HistoricalPublicationCreationReqDTO;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.HistoricalPublicationCreationResDTO;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PublicationResultEnum;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationPublicationErrorException;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDocumentReferenceSRV;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl.IniEdsInvocationSRV;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.PDFUtility;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
// import lombok.extern.slf4j.Slf4j;


// /**
//  *
//  *  Historical document Publication controller.
//  */
// @RestController
// @Slf4j
// public class HistoricalPublicationCTL extends AbstractCTL implements IHistoricalDocPublicationCTL {
//     /**
//      * Serial version uid.
//      */
//     private static final long serialVersionUID = 1711125566058952723L;
    
//     @Autowired
//     private IKafkaSRV kafkaSRV;

// 	@Autowired
//     private IDocumentReferenceSRV documentReferenceSRV;

// 	@Autowired
//     private IniEdsInvocationSRV iniEdsInvocationSRV;
    
    
//     @Override
//     public HistoricalPublicationCreationResDTO historicalDocPublicationCreation(HistoricalPublicationCreationReqDTO requestBody, MultipartFile file, HttpServletRequest request) {
// 		final JWTTokenDTO jwtToken = extractJWT(request.getHeader(Constants.Headers.JWT_HEADER));

//         HistoricalPublicationCreationReqDTO jsonObj = getHistoricalPublicationJSONObject(request.getParameter("requestBody"));

//         byte[] bytePDF = null;
//         PublicationOutputDTO out = null;

//         String cda = "";
//         if (jsonObj==null) {
//         	out = PublicationOutputDTO.builder().msg("I parametri json per la pubblicazione storica devono essere valorizzati.").result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR).build();
//         } else {
//         	if (!jsonObj.isForcePublish()) {
//         		if (jwtToken == null) {
//                     out = PublicationOutputDTO.builder().msg(PublicationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.getTitle()).result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN).build();
//                 } else {
//                     String errorMsg = JWTHeaderDTO.validateHeader(jwtToken.getHeader());
//                     if (errorMsg == null) {
//                         errorMsg = JWTPayloadDTO.validatePayload(jwtToken.getPayload());
//                     }
//                     if (errorMsg != null) {
//                         out = PublicationOutputDTO.builder().msg(errorMsg).result(PublicationResultEnum.INVALID_TOKEN_FIELD).build();
//                     }
//                 }
//         		if(out == null) {
//         			String msgMandatoryElement = checkHistoricalPublicationMandatoryElements(jsonObj);
//         			if (!StringUtility.isNullOrEmpty(msgMandatoryElement)) {
//         				out = PublicationOutputDTO.builder().msg(msgMandatoryElement).result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR).build();
//         			}
        			
//         			if (out == null) {
//         				bytePDF = checkFile(file);
//         				if (bytePDF == null) {
//         					out = PublicationOutputDTO.builder().msg("Il file per la pubblicazione storica deve essere valorizzato").result(PublicationResultEnum.EMPTY_FILE_ERROR).build();
//         				} else {
//         					if (!PDFUtility.isPdf(bytePDF)) {
//         						out = PublicationOutputDTO.builder().msg("Il file per la pubblicazione storica deve essere un PDF").result(PublicationResultEnum.DOCUMENT_TYPE_ERROR).build();
//         					} else {
//         						cda = extractCDA(bytePDF, jsonObj.getMode());

// 								if (!StringUtility.isNullOrEmpty(validateJWT(jwtToken, cda))) {
// 									out = PublicationOutputDTO.builder().msg(PublicationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.getTitle()).result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN).build();
// 								} else {
// 									out = validateCDAHash(cda,jsonObj.getTransactionID());
// 								}
//         					}
//         				}
//         			}

// 					final String documentSha256 = StringUtility.encodeSHA256(bytePDF);
					
        			
//         			if(out == null) {
// 						out = validateDocumentHash(documentSha256, jwtToken);
//         			}

// 					if(out == null) {
//         				String transactionID = jsonObj.getTransactionID();
//         				Integer size = bytePDF!=null ? bytePDF.length : 0;
//         				if(size==0) {
//         					out = PublicationOutputDTO.builder().msg("Attenzione . Il file risulta essere vuoto").result(PublicationResultEnum.DOCUMENT_SIZE_ERROR).build();
//         				}
        				
//         				if(out==null) {
        					
// 							PublicationCreationReqDTO publicationDTO = constructPublicationDTO(jsonObj);
//         					FhirResourceDTO fhirResourcesDTO = documentReferenceSRV.createFhirResources(cda, publicationDTO, size, documentSha256);
//         					if(fhirResourcesDTO!=null &&  !StringUtility.isNullOrEmpty(fhirResourcesDTO.getErrorMessage())) {
//         						out = PublicationOutputDTO.builder().msg(fhirResourcesDTO.getErrorMessage()).result(PublicationResultEnum.FHIR_MAPPING_ERROR).build();
//         					}
        					
//         					if(out == null) {
//         						Boolean isInserted = iniEdsInvocationSRV.insert(transactionID, fhirResourcesDTO, jwtToken);
//         						if(Boolean.TRUE.equals(isInserted)) {
// 									// notify publisher skipping INI invocation
//         							kafkaSRV.notifyPublisher(transactionID);
//         						} else {
//         							log.warn("Attention, insertion of transaction id and document reference not done on mongo");
//         						}
//         					}
//         				}
        				
//         			}
//         		}
//         	}
//         }

//         if(out == null) {
// 			kafkaSRV.sendHistoricalPublicationStatus(EventStatusEnum.SUCCESS, null, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
//         } else {
// 			kafkaSRV.sendHistoricalPublicationStatus(EventStatusEnum.ERROR, out.getMsg(), jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
//         }
        
//         if (out != null) {
//             throw new ValidationPublicationErrorException(out.getResult(), out.getMsg());
//         }
//         return new HistoricalPublicationCreationResDTO(getLogTraceInfo());
//     }
    
// }