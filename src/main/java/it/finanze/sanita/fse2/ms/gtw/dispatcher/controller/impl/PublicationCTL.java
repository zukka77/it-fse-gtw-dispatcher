package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.IPublicationCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTHeaderDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.PublicationOutputDTO;
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
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.facade.ICdaFacadeSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl.IniEdsInvocationSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility;
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
	private ElasticLoggerHelper elasticLogger;
	
	@Autowired
	private ICdaFacadeSRV cdaFacadeSRV;

	@Override
	public PublicationCreationResDTO publicationCreation(PublicationCreationReqDTO requestBody, MultipartFile file, HttpServletRequest request) {

		Date startDateOperation = new Date();

		final JWTTokenDTO jwtToken = extractJWT(request.getHeader(Constants.Headers.JWT_HEADER));

		PublicationCreationReqDTO jsonObj = getPublicationJSONObject(request.getParameter("requestBody"));
		byte[] bytePDF = null;
		PublicationOutputDTO out = null;

		String cda = "";
		
		String workflowInstanceId = "";
		if (jsonObj==null) {
			out = PublicationOutputDTO.builder().msg(PublicationResultEnum.MANDATORY_ELEMENT_ERROR.getTitle()).result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR).build();
		} else {
			workflowInstanceId = jsonObj.getWorkflowInstanceId();
			
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
				String msgMandatoryElement = checkPublicationMandatoryElements(jsonObj);
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
							out = PublicationOutputDTO.builder().msg(PublicationResultEnum.DOCUMENT_TYPE_ERROR.getTitle()).result(PublicationResultEnum.DOCUMENT_TYPE_ERROR).build();
						} else {
							cda = extractCDA(bytePDF, jsonObj.getMode());

							if (Boolean.TRUE.equals(jsonObj.isForcePublish())) {
								workflowInstanceId = CdaUtility.getWorkflowInstanceId(cda);
							}
							
							if (StringUtility.isNullOrEmpty(cda)) {
								out = PublicationOutputDTO.builder().msg(PublicationResultEnum.MINING_CDA_ERROR.getTitle()).result(PublicationResultEnum.MINING_CDA_ERROR).build();
							} else if (!StringUtility.isNullOrEmpty(validateJWT(jwtToken, cda))) {
								out = PublicationOutputDTO.builder().msg(PublicationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.getTitle()).result(PublicationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN).build();
							} else if(!jsonObj.isForcePublish()) {
								out = validateCDAHash(cda, workflowInstanceId);
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
						out = PublicationOutputDTO.builder().msg("Attenzione . Il file risulta essere vuoto").result(PublicationResultEnum.DOCUMENT_SIZE_ERROR).build();
					}

					if(out==null) {

						FhirResourceDTO fhirResourcesDTO = documentReferenceSRV.createFhirResources(cda, jsonObj, size, documentSha256);
						if(fhirResourcesDTO!=null &&  !StringUtility.isNullOrEmpty(fhirResourcesDTO.getErrorMessage())) {
							out = PublicationOutputDTO.builder().msg(fhirResourcesDTO.getErrorMessage()).result(PublicationResultEnum.FHIR_MAPPING_ERROR).build();
						}

						if(out == null) {
							Boolean isInserted = iniInvocationSRV.insert(workflowInstanceId, fhirResourcesDTO, jwtToken);
							if(Boolean.TRUE.equals(isInserted)) {
								kafkaSRV.notifyIndexer(workflowInstanceId);
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
			kafkaSRV.sendPublicationStatus(EventStatusEnum.SUCCESS, null, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
		} else {
			kafkaSRV.sendPublicationStatus(EventStatusEnum.ERROR, out.getMsg(), jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
		}

		if (out!=null) {
			String txId = workflowInstanceId != null ? workflowInstanceId : "UNKNOW TX ID";
			elasticLogger.error(out.getMsg() + " " + txId, OperationLogEnum.PUB_CDA2, ResultLogEnum.KO, startDateOperation, out.getResult().getErrorCategory());
			throw new ValidationPublicationErrorException(out.getResult(), out.getMsg());
		}

		elasticLogger.info(String.format("Publication CDA completed for workflow instance id %s", workflowInstanceId != null ? workflowInstanceId : "UNKNOW TX ID"), OperationLogEnum.PUB_CDA2, ResultLogEnum.OK, startDateOperation);
		return new PublicationCreationResDTO(getLogTraceInfo());
	}
}