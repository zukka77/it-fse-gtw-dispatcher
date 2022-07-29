package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.IPublicationCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationCreationInputDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationMetadataReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.PublicationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ResponseDTO;
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
import lombok.extern.slf4j.Slf4j;
/**
 *
 * @author CPIERASC
 *
 *  Publication controller.
 */
@Slf4j
@RestController
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
	private transient ElasticLoggerHelper elasticLogger;

	@Autowired
	private ICdaFacadeSRV cdaSRV;

	@Autowired
	private IErrorHandlerSRV errorHandlerSRV;

	@Override
	public ResponseEntity<PublicationResDTO> create(final PublicationCreationReqDTO requestBody, final MultipartFile file, final HttpServletRequest request) {
		final Date startDateOperation = new Date();
		final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();

		ValidationCreationInputDTO validationInfo = new ValidationCreationInputDTO();
		validationInfo.setValidationData(new ValidationDataDTO(null, false, Constants.App.MISSING_WORKFLOW_PLACEHOLDER));

		try {
			validationInfo = validateInput(file, request, false);

			if (validationInfo.getValidationError() != null) {
				throw validationInfo.getValidationError();
			}

			iniInvocationSRV.insert(validationInfo.getValidationData().getWorkflowInstanceId(), validationInfo.getFhirResource(), validationInfo.getJwtToken());
			
			PriorityTypeEnum priorityType = PriorityTypeEnum.NULL;
			if (validationInfo.getJsonObj().getPriorita() != null) {
				priorityType = Boolean.TRUE.equals(validationInfo.getJsonObj().getPriorita()) ? PriorityTypeEnum.HIGH : PriorityTypeEnum.LOW;
			}

			final IndexerValueDTO kafkaValue = new IndexerValueDTO(validationInfo.getValidationData().getWorkflowInstanceId(), null);

			kafkaSRV.notifyChannel(validationInfo.getKafkaKey(), new Gson().toJson(kafkaValue), priorityType, validationInfo.getJsonObj().getTipoDocumentoLivAlto(), DestinationTypeEnum.INDEXER);
			kafkaSRV.sendPublicationStatus(traceInfoDTO.getTraceID(), validationInfo.getValidationData().getWorkflowInstanceId(), EventStatusEnum.SUCCESS, null, validationInfo.getJsonObj(), validationInfo.getJwtToken() != null ? validationInfo.getJwtToken().getPayload() : null);
			
			elasticLogger.info(String.format("Publication CDA completed for workflow instance id %s", validationInfo.getValidationData().getWorkflowInstanceId()), OperationLogEnum.PUB_CDA2, ResultLogEnum.OK, startDateOperation, validationInfo.getJwtToken().getPayload().getIss());
		} catch (ConnectionRefusedException ce) {
			errorHandlerSRV.connectionRefusedExceptionHandler(startDateOperation, validationInfo.getValidationData(), validationInfo.getJwtToken(), validationInfo.getJsonObj(), traceInfoDTO, ce, false);
		} catch (final ValidationException e) {
			errorHandlerSRV.publicationValidationExceptionHandler(startDateOperation, validationInfo.getValidationData(), validationInfo.getJwtToken(), validationInfo.getJsonObj(), traceInfoDTO, e, false);
		}

		String warning = null;
		
		if (validationInfo.getJsonObj().getMode() == null) {
			warning = Constants.Misc.WARN_EXTRACTION_SELECTION;
		}
		
		return new ResponseEntity<>(new PublicationResDTO(traceInfoDTO, warning, validationInfo.getValidationData().getWorkflowInstanceId()), HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<PublicationResDTO> replace(final String identificativoDocUpdate, final PublicationUpdateReqDTO requestBody, 
		final MultipartFile file, final HttpServletRequest request) {
		
			final Date startDateOperation = new Date();
			final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();

			ValidationCreationInputDTO validationInfo = new ValidationCreationInputDTO();
			validationInfo.setValidationData(new ValidationDataDTO(null, false, Constants.App.MISSING_WORKFLOW_PLACEHOLDER));

			try {
				
				validationInfo = validateInput(file, request, true);

				if (validationInfo.getValidationError() != null) {
					throw validationInfo.getValidationError();
				}

				log.info("Executing replace of document: {}", identificativoDocUpdate);
				iniInvocationSRV.replace(validationInfo.getValidationData().getWorkflowInstanceId(), validationInfo.getFhirResource(), validationInfo.getJwtToken(), identificativoDocUpdate);
				
				final IndexerValueDTO kafkaValue = new IndexerValueDTO(validationInfo.getValidationData().getWorkflowInstanceId(), identificativoDocUpdate);
				
				kafkaSRV.notifyChannel(validationInfo.getKafkaKey(), new Gson().toJson(kafkaValue), PriorityTypeEnum.LOW, validationInfo.getJsonObj().getTipoDocumentoLivAlto(), DestinationTypeEnum.INDEXER);
				kafkaSRV.sendReplaceStatus(traceInfoDTO.getTraceID(), validationInfo.getValidationData().getWorkflowInstanceId(), EventStatusEnum.SUCCESS, null, validationInfo.getJsonObj(), validationInfo.getJwtToken() != null ? validationInfo.getJwtToken().getPayload() : null);

				elasticLogger.info(String.format("Publication CDA completed for workflow instance id %s", validationInfo.getValidationData().getWorkflowInstanceId()), OperationLogEnum.PUB_CDA2, ResultLogEnum.OK, startDateOperation, validationInfo.getJwtToken().getPayload().getIss());
			} catch (ConnectionRefusedException ce) {
				errorHandlerSRV.connectionRefusedExceptionHandler(startDateOperation, validationInfo.getValidationData(), validationInfo.getJwtToken(), validationInfo.getJsonObj(), traceInfoDTO, ce, true);
			} catch (final ValidationException e) {
				errorHandlerSRV.publicationValidationExceptionHandler(startDateOperation, validationInfo.getValidationData(), validationInfo.getJwtToken(), validationInfo.getJsonObj(), traceInfoDTO, e, true);
			}
	
			String warning = null;
			
			if (validationInfo.getJsonObj().getMode() == null) {
				warning = "Attenzione , non è stata selezionata la modalità di estrazione del CDA";
			}
			
			return new ResponseEntity<>(new PublicationResDTO(traceInfoDTO, warning, validationInfo.getValidationData().getWorkflowInstanceId()), HttpStatus.CREATED);
	}


	@Override
	public ResponseEntity<ResponseDTO> replaceMetadata(final String identificativoDoc,
			final PublicationMetadataReqDTO requestBody, final HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	private ValidationCreationInputDTO validateInput(final MultipartFile file, final HttpServletRequest request, final boolean isReplace) {

		final ValidationCreationInputDTO validation = new ValidationCreationInputDTO();
		ValidationDataDTO validationInfo = new ValidationDataDTO();
		validationInfo.setCdaValidated(false);
		validationInfo.setWorkflowInstanceId(Constants.App.MISSING_WORKFLOW_PLACEHOLDER);
		
		validation.setValidationData(validationInfo);

		try {
			if (Boolean.TRUE.equals(msCfg.getFromGovway())) {
				final JWTTokenDTO jwtToken = extractAndValidateJWT(request.getHeader(Constants.Headers.JWT_GOVWAY_HEADER), msCfg.getFromGovway());
				validation.setJwtToken(jwtToken);
			} else {
				final JWTTokenDTO jwtToken = extractAndValidateJWT(request.getHeader(Constants.Headers.JWT_HEADER), msCfg.getFromGovway());
				validation.setJwtToken(jwtToken);
			}
			
			PublicationCreationReqDTO jsonObj = getAndValidatePublicationReq(request.getParameter("requestBody"), isReplace);
			validation.setJsonObj(jsonObj);

			final byte[] bytePDF = getAndValidateFile(file);
			validation.setFile(bytePDF);
			
			final String cda = extractCDA(bytePDF, jsonObj.getMode());
			validation.setCda(cda);
			
			validateJWT(validation.getJwtToken(), cda);
			
			final org.jsoup.nodes.Document docT = Jsoup.parse(cda);
			final String key = CdaUtility.extractFieldCda(docT);
			validation.setDocument(docT);
			validation.setKafkaKey(key);
	
			if (!Boolean.TRUE.equals(jsonObj.isForcePublish()) && !isReplace) {
				validationInfo = getValidationInfo(cda, jsonObj.getWorkflowInstanceId());
			} else {
				validationInfo.setWorkflowInstanceId(CdaUtility.getWorkflowInstanceId(docT));
			}

			if (isReplace) {
				log.info("Validating CDA for replace");
				validate(cda, ActivityEnum.REPLACE, validation.getValidationData().getWorkflowInstanceId());
			}
	
			validation.setValidationData(validationInfo); // Updating validation info

			if (!Boolean.TRUE.equals(jsonObj.isForcePublish()) && !isReplace) {
				cdaSRV.consumeHash(validationInfo.getHash());
			}
			
			final String documentSha256 = StringUtility.encodeSHA256(bytePDF);
			validation.setDocumentSha(documentSha256);
	
			validateDocumentHash(documentSha256, validation.getJwtToken());
	
			final ResourceDTO fhirResourcesDTO = documentReferenceSRV.createFhirResources(cda, jsonObj, bytePDF.length, documentSha256,
				validation.getJwtToken().getPayload().getPerson_id());
	
			validation.setFhirResource(fhirResourcesDTO);
			if(!StringUtility.isNullOrEmpty(fhirResourcesDTO.getErrorMessage())) {
				final ErrorResponseDTO error = ErrorResponseDTO.builder()
					.type(RestExecutionResultEnum.FHIR_MAPPING_ERROR.getType())
					.title(RestExecutionResultEnum.FHIR_MAPPING_ERROR.getTitle())
					.instance(ErrorInstanceEnum.FHIR_RESOURCE_ERROR.getInstance())
					.detail(RestExecutionResultEnum.FHIR_MAPPING_ERROR.getTitle()).build();
	
				throw new ValidationException(error);
			}
		} catch (final ValidationException ve) {
			cdaSRV.consumeHash(validationInfo.getHash());
			validation.setValidationError(ve);
		}

		return validation;
	}

}
