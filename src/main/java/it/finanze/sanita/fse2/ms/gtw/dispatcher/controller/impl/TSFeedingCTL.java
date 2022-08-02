package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.ITSFeedingCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.TSPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TSPublicationCreationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.DestinationTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ErrorInstanceEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.logging.ElasticLoggerHelper;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDocumentReferenceSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IErrorHandlerSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.facade.ICdaFacadeSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl.IniEdsInvocationSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;

/**
 * 
 * TS document feeding controller.
 */
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
	private transient ElasticLoggerHelper elasticLogger;

	@Autowired
	private ICdaFacadeSRV cdaSRV;

	@Autowired
	private transient IErrorHandlerSRV errorHandlerSRV;

	@Override
	public TSPublicationCreationResDTO tsFeeding(final TSPublicationCreationReqDTO requestBody,
			final MultipartFile file, final HttpServletRequest request) {

		final Date startDateOperation = new Date();
		String workflowInstanceId = Constants.App.MISSING_WORKFLOW_PLACEHOLDER;
		
		JWTTokenDTO jwtToken = null;
		TSPublicationCreationReqDTO jsonObj = null;
		LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();
		try {
			if (Boolean.TRUE.equals(msCfg.getFromGovway())) {
				jwtToken = extractAndValidateJWT(request.getHeader(Constants.Headers.JWT_GOVWAY_HEADER), msCfg.getFromGovway());
			} else {
				jwtToken = extractAndValidateJWT(request.getHeader(Constants.Headers.JWT_HEADER), msCfg.getFromGovway());
			}
			jsonObj = StringUtility.fromJSONJackson(request.getParameter("requestBody"), TSPublicationCreationReqDTO.class);
			validateTSPublicationReq(jsonObj);

			final byte[] bytePDF = getAndValidateFile(file);
			final String cda = extractCDA(bytePDF, jsonObj.getMode());

			org.jsoup.nodes.Document docT = Jsoup.parse(cda);
			workflowInstanceId = CdaUtility.getWorkflowInstanceId(docT);

			validateJWT(jwtToken, cda);

			if (!jsonObj.isForcePublish()) {
				validate(cda, ActivityEnum.TS_PRE_PUBLISHING, workflowInstanceId);
			}

			final String documentSha256 = StringUtility.encodeSHA256(bytePDF);
			validateDocumentHash(documentSha256, jwtToken);

			final ResourceDTO fhirResourcesDTO = documentReferenceSRV.createFhirResources(cda,
					constructPublicationDTO(jsonObj), bytePDF.length, documentSha256,jwtToken.getPayload().getPerson_id());

			if (!StringUtility.isNullOrEmpty(fhirResourcesDTO.getErrorMessage())) {
				final ErrorResponseDTO error = ErrorResponseDTO.builder()
						.type(RestExecutionResultEnum.FHIR_MAPPING_ERROR.getType())
						.title(RestExecutionResultEnum.FHIR_MAPPING_ERROR.getTitle())
						.instance(ErrorInstanceEnum.FHIR_RESOURCE_ERROR.getInstance())
						.detail(RestExecutionResultEnum.FHIR_MAPPING_ERROR.getTitle()).build();

				throw new ValidationException(error);
			}

			iniEdsInvocationSRV.insert(workflowInstanceId, fhirResourcesDTO, jwtToken);
			
			String key = CdaUtility.extractFieldCda(docT);

			PriorityTypeEnum priorityType = PriorityTypeEnum.NULL;
			if (jsonObj.getPriorita() != null) {
				priorityType = Boolean.TRUE.equals(jsonObj.getPriorita()) ? PriorityTypeEnum.HIGH : PriorityTypeEnum.LOW;
			}

			final IndexerValueDTO kafkaValue = new IndexerValueDTO(workflowInstanceId, null);

			kafkaSRV.notifyChannel(key, new Gson().toJson(kafkaValue), priorityType, jsonObj.getTipoDocumentoLivAlto(), DestinationTypeEnum.PUBLISHER);
			kafkaSRV.sendFeedingStatus(traceInfoDTO.getTraceID(), workflowInstanceId, EventStatusEnum.SUCCESS, null, jsonObj, jwtToken.getPayload());

			if (!Boolean.TRUE.equals(jsonObj.isForcePublish())) {
				cdaSRV.consumeHash(workflowInstanceId);
			}
			elasticLogger.info(String.format("Publication CDA received from TS completed for workflow instance id %s", workflowInstanceId), OperationLogEnum.PUB_CDA2, ResultLogEnum.OK, startDateOperation,
					jwtToken.getPayload().getIss());

		} catch (ValidationException e) {
			errorHandlerSRV.tsFeedingValidationExceptionHandler(startDateOperation, workflowInstanceId, jwtToken, jsonObj, traceInfoDTO, e);
		} catch (ConnectionRefusedException ex) {
			errorHandlerSRV.tsFeedingConnectionRefusedExceptionHandler(startDateOperation, workflowInstanceId, jwtToken, jsonObj, traceInfoDTO, ex);
		}
		
		String warning = null;

		if (jsonObj != null && jsonObj.getMode() == null) {
			warning = Constants.Misc.WARN_EXTRACTION_SELECTION;
		}
		
		return new TSPublicationCreationResDTO(traceInfoDTO, workflowInstanceId, warning);
	}
}
