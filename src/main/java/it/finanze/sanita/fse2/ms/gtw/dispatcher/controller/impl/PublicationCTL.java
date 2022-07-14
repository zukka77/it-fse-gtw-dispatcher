package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.IPublicationCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTHeaderDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.PublicationOutputDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.PublicationCreationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationPublicationErrorException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.logging.ElasticLoggerHelper;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDocumentReferenceSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl.IniEdsInvocationSRV;
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

	@Autowired
	private IKafkaSRV kafkaSRV;

	@Autowired
	private IDocumentReferenceSRV documentReferenceSRV;

	@Autowired
	private IniEdsInvocationSRV iniInvocationSRV;

	@Autowired
	private MicroservicesURLCFG msCfg;
	
	@Autowired
	private ElasticLoggerHelper elasticLogger;

	@Override
	public ResponseEntity<PublicationCreationResDTO> publicationCreation(PublicationCreationReqDTO requestBody, MultipartFile file, HttpServletRequest request) {
		Date startDateOperation = new Date();
		ValidationDataDTO validationInfo = new ValidationDataDTO();
		validationInfo.setCdaValidated(false);
		validationInfo.setWorkflowInstanceId(Constants.App.MISSING_WORKFLOW_PLACEHOLDER);

		JWTTokenDTO jwtToken = null;
		if (Boolean.TRUE.equals(msCfg.getFromGovway())) {
			jwtToken = extractJWT(request.getHeader(Constants.Headers.JWT_GOVWAY_HEADER));
		} else {
			jwtToken = extractJWT(request.getHeader(Constants.Headers.JWT_HEADER));
		}
		
		PublicationCreationReqDTO jsonObj = getPublicationJSONObject(request.getParameter("requestBody"));
		byte[] bytePDF = null;
		PublicationOutputDTO out = null;

		String cda = "";
		if (jsonObj==null) {
			out = PublicationOutputDTO.builder().msg("I parametri json devono essere valorizzati.").result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR).build();
		} else {
			if (jwtToken == null) {
				out = PublicationOutputDTO.builder().msg(PublicationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.getTitle()).result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN).build();
			} else {
				String errorMsg = null;
				if (!Boolean.TRUE.equals(msCfg.getFromGovway())) {
					errorMsg = JWTHeaderDTO.validateHeader(jwtToken.getHeader());
				}
				if (errorMsg == null) {
					errorMsg = JWTPayloadDTO.validatePayload(jwtToken.getPayload());
				}
				if (errorMsg != null) {
					out = PublicationOutputDTO.builder().msg(errorMsg).result(PublicationResultEnum.INVALID_TOKEN_FIELD).build();
				}
			}

			if(out == null) {
				String msgMandatoryElement = checkPublicationMandatoryElements(jsonObj);
				if (!StringUtility.isNullOrEmpty(msgMandatoryElement)) {
					out = PublicationOutputDTO.builder().msg(msgMandatoryElement).result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR).build();
				} else {
					String checkDateFormat = checkFormatDate(jsonObj.getDataInizioPrestazione(),jsonObj.getDataFinePrestazione());
					if (!StringUtility.isNullOrEmpty(checkDateFormat)) {
						out = PublicationOutputDTO.builder().msg(checkDateFormat).result(PublicationResultEnum.FORMAT_ELEMENT_ERROR).build();
					}
				}

				if (out == null) {
					bytePDF = checkFile(file);
					if (bytePDF == null) {
						out = PublicationOutputDTO.builder().msg("Il file deve essere valorizzato").result(PublicationResultEnum.EMPTY_FILE_ERROR).build();
					} else {
						if (!PDFUtility.isPdf(bytePDF)) {
							out = PublicationOutputDTO.builder().msg("Il file deve essere un PDF").result(PublicationResultEnum.DOCUMENT_TYPE_ERROR).build();
						} else {
							cda = extractCDA(bytePDF, jsonObj.getMode());

							if (!StringUtility.isNullOrEmpty(validateJWT(jwtToken, cda))) {
								out = PublicationOutputDTO.builder().msg(PublicationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.getTitle()).result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN).build();
							} else /**if(!jsonObj.isForcePublish())**/ {
								validationInfo = getValidationInfo(cda, jsonObj.getWorkflowInstanceId());

								if (!validationInfo.isCdaValidated()) {
									out = PublicationOutputDTO.builder().msg("Il CDA non risulta validato").result(PublicationResultEnum.CDA_MATCH_ERROR).build();
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
					final Integer size = bytePDF!=null ? bytePDF.length : 0;
					if(size==0) {
						out = PublicationOutputDTO.builder().msg("Attenzione. Il file risulta essere vuoto").result(PublicationResultEnum.DOCUMENT_SIZE_ERROR).build();
					}

				// 	if(out==null) {

				// 		FhirResourceDTO fhirResourcesDTO = documentReferenceSRV.createFhirResources(cda, jsonObj, size, documentSha256,
				// 				jwtToken.getPayload().getPerson_id());
				// 		if(fhirResourcesDTO!=null &&  !StringUtility.isNullOrEmpty(fhirResourcesDTO.getErrorMessage())) {
				// 			out = PublicationOutputDTO.builder().msg(fhirResourcesDTO.getErrorMessage()).result(PublicationResultEnum.FHIR_MAPPING_ERROR).build();
				// 		}

				// 		if(out == null) {
				// 			Boolean isInserted = iniInvocationSRV.insert(validationInfo.getWorkflowInstanceId(), fhirResourcesDTO, jwtToken);
				// 			if(Boolean.TRUE.equals(isInserted)) {
				// 				kafkaSRV.notifyIndexer(validationInfo.getWorkflowInstanceId());
				// 			} else {
				// 				log.warn("Attention, insertion of transaction id and document reference not done on mongo");
				// 			}
				// 		}
				// 	}

				}
			}
		}

		if(out==null){
			kafkaSRV.sendPublicationStatus(getLogTraceInfo().getTraceID(), EventStatusEnum.SUCCESS, null, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
		} else {
			kafkaSRV.sendPublicationStatus(getLogTraceInfo().getTraceID(), EventStatusEnum.ERROR, out.getMsg(), jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
		}

		if (out!=null) {
			final String issuer = jwtToken != null && jwtToken.getPayload() != null ? jwtToken.getPayload().getIss() : null;
			elasticLogger.error(out.getMsg() + " " + validationInfo.getWorkflowInstanceId(), OperationLogEnum.PUB_CDA2, ResultLogEnum.KO, startDateOperation, out.getResult().getErrorCategory(), issuer);
			throw new ValidationPublicationErrorException(out.getResult(), out.getMsg());
		}

		elasticLogger.info(String.format("Publication CDA completed for transactionID %s", validationInfo.getWorkflowInstanceId()), OperationLogEnum.PUB_CDA2, ResultLogEnum.OK, startDateOperation, jwtToken.getPayload().getIss());
		return new ResponseEntity<>(new PublicationCreationResDTO(getLogTraceInfo(), null), HttpStatus.CREATED);
	}
}