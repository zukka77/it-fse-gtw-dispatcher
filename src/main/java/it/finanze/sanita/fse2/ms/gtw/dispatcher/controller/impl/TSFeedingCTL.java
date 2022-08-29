package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import javax.servlet.http.HttpServletRequest;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Hidden;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.ITSFeedingCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.TSPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TSPublicationCreationResDTO;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.logging.ElasticLoggerHelper;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDocumentReferenceSRV;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IErrorHandlerSRV;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.facade.ICdaFacadeSRV;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl.IniEdsInvocationSRV;

/**
 * 
 * TS document feeding controller.
 */
@RestController
@Hidden
public class TSFeedingCTL extends AbstractCTL implements ITSFeedingCTL {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -8596007149594204537L;

	// @Autowired
	// private IDocumentReferenceSRV documentReferenceSRV;

	// @Autowired
	// private IniEdsInvocationSRV iniEdsInvocationSRV;

	// @Autowired
	// private IKafkaSRV kafkaSRV;

	// @Autowired
	// private transient ElasticLoggerHelper elasticLogger;

	// @Autowired
	// private ICdaFacadeSRV cdaSRV;

	// @Autowired
	// private transient IErrorHandlerSRV errorHandlerSRV;

	@Override
	public TSPublicationCreationResDTO tsFeeding(final TSPublicationCreationReqDTO requestBody,
			final MultipartFile file, final HttpServletRequest request) {

//		final Date startDateOperation = new Date();
//		String workflowInstanceId = Constants.App.MISSING_WORKFLOW_PLACEHOLDER;
//		
//		JWTTokenDTO jwtToken = null;
//		TSPublicationCreationReqDTO jsonObj = null;
//		LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();
//		try {
//			if (Boolean.TRUE.equals(msCfg.getFromGovway())) {
//				jwtToken = extractAndValidateJWT(request.getHeader(Constants.Headers.JWT_GOVWAY_HEADER), msCfg.getFromGovway());
//			} else {
//				jwtToken = extractAndValidateJWT(request.getHeader(Constants.Headers.JWT_HEADER), msCfg.getFromGovway());
//			}
//			jsonObj = StringUtility.fromJSONJackson(request.getParameter("requestBody"), TSPublicationCreationReqDTO.class);
//			validateTSPublicationReq(jsonObj);
//
//			final byte[] bytePDF = getAndValidateFile(file);
//			final String cda = extractCDA(bytePDF, jsonObj.getMode());
//
//			org.jsoup.nodes.Document docT = Jsoup.parse(cda);
//			workflowInstanceId = CdaUtility.getWorkflowInstanceId(docT);
//
//			validateJWT(jwtToken, cda);
//
//			if (!jsonObj.isForcePublish()) {
//				validate(cda, ActivityEnum.TS_PRE_PUBLISHING, workflowInstanceId);
//			}
//
//			final String documentSha256 = StringUtility.encodeSHA256(bytePDF);
//			validateDocumentHash(documentSha256, jwtToken);
//
//			final ResourceDTO fhirResourcesDTO = documentReferenceSRV.createFhirResources(cda,
//					constructPublicationDTO(jsonObj), bytePDF.length, documentSha256,jwtToken.getPayload().getPerson_id());
//
//			if (!StringUtility.isNullOrEmpty(fhirResourcesDTO.getErrorMessage())) {
//				final ErrorResponseDTO error = ErrorResponseDTO.builder()
//						.type(RestExecutionResultEnum.FHIR_MAPPING_ERROR.getType())
//						.title(RestExecutionResultEnum.FHIR_MAPPING_ERROR.getTitle())
//						.instance(ErrorInstanceEnum.FHIR_RESOURCE_ERROR.getInstance())
//						.detail(RestExecutionResultEnum.FHIR_MAPPING_ERROR.getTitle()).build();
//
//				throw new ValidationException(error);
//			}
//
//			iniEdsInvocationSRV.insert(workflowInstanceId, fhirResourcesDTO, jwtToken);
//			
//			String key = CdaUtility.extractFieldCda(docT);
//
//			PriorityTypeEnum priorityType = PriorityTypeEnum.NULL;
//			if (jsonObj.getPriorita() != null) {
//				priorityType = Boolean.TRUE.equals(jsonObj.getPriorita()) ? PriorityTypeEnum.HIGH : PriorityTypeEnum.LOW;
//			}
//
//			final IndexerValueDTO kafkaValue = new IndexerValueDTO(workflowInstanceId, null);
//
//			kafkaSRV.notifyChannel(key, new Gson().toJson(kafkaValue), priorityType, jsonObj.getTipoDocumentoLivAlto(), DestinationTypeEnum.PUBLISHER);
//			kafkaSRV.sendFeedingStatus(traceInfoDTO.getTraceID(), workflowInstanceId, EventStatusEnum.SUCCESS, null, jsonObj, jwtToken.getPayload());
//
//			if (!Boolean.TRUE.equals(jsonObj.isForcePublish())) {
//				cdaSRV.consumeHash(workflowInstanceId);
//			}
//			elasticLogger.info(String.format("Publication CDA received from TS completed for workflow instance id %s", workflowInstanceId), OperationLogEnum.PUB_CDA2, ResultLogEnum.OK, startDateOperation,
//					jwtToken.getPayload().getIss());
//
//		} catch (ValidationException e) {
//			errorHandlerSRV.tsFeedingValidationExceptionHandler(startDateOperation, workflowInstanceId, jwtToken, jsonObj, traceInfoDTO, e);
//		} catch (ConnectionRefusedException ex) {
//			errorHandlerSRV.tsFeedingConnectionRefusedExceptionHandler(startDateOperation, workflowInstanceId, jwtToken, jsonObj, traceInfoDTO, ex);
//		}
//		
//		String warning = null;
//
//		if (jsonObj != null && jsonObj.getMode() == null) {
//			warning = Constants.Misc.WARN_EXTRACTION_SELECTION;
//		}
//		
//		return new TSPublicationCreationResDTO(traceInfoDTO, workflowInstanceId, warning);
		return new TSPublicationCreationResDTO();
	}
}
