/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants.App.MISSING_DOC_TYPE_PLACEHOLDER;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants.App.MISSING_WORKFLOW_PLACEHOLDER;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum.BLOCKING_ERROR;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum.SUCCESS;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventTypeEnum.EDS_DELETE;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventTypeEnum.EDS_UPDATE;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventTypeEnum.INI_DELETE;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventTypeEnum.INI_UPDATE;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventTypeEnum.RIFERIMENTI_INI;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum.FHIR_MAPPING_ERROR;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum.INI_EXCEPTION;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum.get;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility.createMasterIdError;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility.createReqMasterIdError;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility.createWorkflowInstanceId;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility.extractFieldCda;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility.getDocumentType;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility.isValidMasterId;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility.encodeSHA256;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility.isNullOrEmpty;

import java.util.Date;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Size;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IIniClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.AccreditationSimulationCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants.Misc;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.ValidationCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.IPublicationCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.AccreditamentoSimulationDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationCreationInputDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.DeleteRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.IniMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.IniReferenceRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.MergedMetadatiRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationFatherCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationMetadataReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ConversionResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.GetMergedMetadatiDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniReferenceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniTraceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.PublicationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ResponseWifDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.TransformResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.DestinationTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ErrorInstanceEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.EdsException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.IniException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.MockEnabledException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.NoRecordFoundException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.logging.LoggerHelper;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IAccreditamentoSimulationSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDocumentReferenceSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IErrorHandlerSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.facade.ICdaFacadeSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl.IniEdsInvocationSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.ValidationUtility;
import lombok.extern.slf4j.Slf4j;

/**
 *  Publication controller.
 */
@Slf4j
@RestController
public class PublicationCTL extends AbstractCTL implements IPublicationCTL {

	@Autowired
	private IKafkaSRV kafkaSRV;

	@Autowired
	private IDocumentReferenceSRV documentReferenceSRV;

	@Autowired
	private IniEdsInvocationSRV iniInvocationSRV;

	@Autowired
	private LoggerHelper logger;

	@Autowired
	private ICdaFacadeSRV cdaSRV;

	@Autowired
	private IErrorHandlerSRV errorHandlerSRV;

	@Autowired
	private IIniClient iniClient;

	@Autowired
	private IEdsClient edsClient;

	@Autowired
	private ValidationCFG validationCFG;

	@Autowired
	private IAccreditamentoSimulationSRV accreditamentoSimulationSRV;

	@Autowired
	private AccreditationSimulationCFG accreditationSimulationCFG;


	@Override
	public ResponseEntity<PublicationResDTO> create(final PublicationCreationReqDTO requestBody, final MultipartFile file, final HttpServletRequest request) {
		final Date startDateOperation = new Date();
		final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();

		log.info("[START] {}() with arguments {}={}, {}={}, {}={}","create","traceId", traceInfoDTO.getTraceID(),"wif", requestBody.getWorkflowInstanceId(),"idDoc", requestBody.getIdentificativoDoc());

		ValidationCreationInputDTO validationInfo = new ValidationCreationInputDTO();
		validationInfo.setValidationData(new ValidationDataDTO(null, false, MISSING_WORKFLOW_PLACEHOLDER, null, null, new Date()));

		try {
			validationInfo = publicationAndReplace(file, request, false,null,traceInfoDTO);

			postExecutionCreate(startDateOperation, traceInfoDTO, validationInfo);
		} catch (ConnectionRefusedException ce) {
			errorHandlerSRV.connectionRefusedExceptionHandler(startDateOperation, validationInfo.getValidationData(), validationInfo.getJwtPayloadToken(), validationInfo.getJsonObj(), traceInfoDTO, ce, true, getDocumentType(validationInfo.getDocument()));
		} catch (final ValidationException e) {
			errorHandlerSRV.publicationValidationExceptionHandler(startDateOperation, validationInfo.getValidationData(), validationInfo.getJwtPayloadToken(), validationInfo.getJsonObj(), traceInfoDTO, e, true, getDocumentType(validationInfo.getDocument()));
		}

		String warning = null;

		if (validationInfo.getJsonObj().getMode() == null) {
			warning = Misc.WARN_EXTRACTION_SELECTION;
		}

		log.info("[EXIT] {}() with arguments {}={}, {}={}, {}={}","create","traceId", traceInfoDTO.getTraceID(),"wif", requestBody.getWorkflowInstanceId(),"idDoc", requestBody.getIdentificativoDoc());

		return new ResponseEntity<>(new PublicationResDTO(traceInfoDTO, warning, validationInfo.getValidationData().getWorkflowInstanceId()), HttpStatus.CREATED);
	}

	private void postExecutionCreate(final Date startDateOperation, final LogTraceInfoDTO traceInfoDTO,
			ValidationCreationInputDTO validationInfo) {
		iniInvocationSRV.insert(validationInfo.getValidationData().getWorkflowInstanceId(), validationInfo.getFhirResource(), validationInfo.getJwtPayloadToken());

		PriorityTypeEnum priorityType = PriorityTypeEnum.NULL;
		if (validationInfo.getJsonObj().getPriorita() != null) {
			priorityType = Boolean.TRUE.equals(validationInfo.getJsonObj().getPriorita()) ? PriorityTypeEnum.HIGH : PriorityTypeEnum.LOW;
		}

		final IndexerValueDTO kafkaValue = new IndexerValueDTO();
		kafkaValue.setWorkflowInstanceId(validationInfo.getValidationData().getWorkflowInstanceId());
		kafkaValue.setIdDoc(validationInfo.getJsonObj().getIdentificativoDoc());
		kafkaValue.setEdsDPOperation(ProcessorOperationEnum.PUBLISH);

		kafkaSRV.notifyChannel(validationInfo.getKafkaKey(), new Gson().toJson(kafkaValue), priorityType, validationInfo.getJsonObj().getTipoDocumentoLivAlto(), DestinationTypeEnum.INDEXER);
		kafkaSRV.sendPublicationStatus(traceInfoDTO.getTraceID(), validationInfo.getValidationData().getWorkflowInstanceId(), SUCCESS, null, validationInfo.getJsonObj(), validationInfo.getJwtPayloadToken());

		logger.info(Constants.App.LOG_TYPE_CONTROL,validationInfo.getValidationData().getWorkflowInstanceId(),String.format("Publication CDA completed for workflow instance id %s", validationInfo.getValidationData().getWorkflowInstanceId()), OperationLogEnum.PUB_CDA2, ResultLogEnum.OK, startDateOperation, getDocumentType(validationInfo.getDocument()),
				validationInfo.getJwtPayloadToken());
	}

	@Override
	public ResponseEntity<PublicationResDTO> replace(final String idDoc, final PublicationUpdateReqDTO requestBody, final MultipartFile file, final HttpServletRequest request) {

		final Date startDateOperation = new Date();
		final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();

		ValidationCreationInputDTO validationInfo = new ValidationCreationInputDTO();
		validationInfo.setValidationData(new ValidationDataDTO(null, false, MISSING_WORKFLOW_PLACEHOLDER, null, null, new Date()));

		try {
			if(!isValidMasterId(idDoc)) throw new ValidationException(createReqMasterIdError());
			validationInfo = publicationAndReplace(file, request, true,idDoc,traceInfoDTO);
 
			log.info("[START] {}() with arguments {}={}, {}={}, {}={}","replace","traceId", traceInfoDTO.getTraceID(),"wif", validationInfo.getValidationData().getWorkflowInstanceId(),"idDoc", idDoc);

			IniReferenceRequestDTO iniReq = new IniReferenceRequestDTO(idDoc, validationInfo.getJwtPayloadToken());
			IniReferenceResponseDTO response = iniClient.reference(iniReq);

			if(!isNullOrEmpty(response.getErrorMessage())) {
				log.error("Errore. Nessun riferimento trovato.");
				throw new IniException(response.getErrorMessage());
			}


			log.debug("Executing replace of document: {}", idDoc);
			iniInvocationSRV.replace(validationInfo.getValidationData().getWorkflowInstanceId(), validationInfo.getFhirResource(), validationInfo.getJwtPayloadToken(), response.getUuid());

			final IndexerValueDTO kafkaValue = new IndexerValueDTO();
			kafkaValue.setWorkflowInstanceId(validationInfo.getValidationData().getWorkflowInstanceId());
			kafkaValue.setIdDoc(idDoc);
			kafkaValue.setEdsDPOperation(ProcessorOperationEnum.REPLACE);

			kafkaSRV.notifyChannel(validationInfo.getKafkaKey(), new Gson().toJson(kafkaValue), PriorityTypeEnum.LOW, validationInfo.getJsonObj().getTipoDocumentoLivAlto(), DestinationTypeEnum.INDEXER);
			kafkaSRV.sendReplaceStatus(traceInfoDTO.getTraceID(), validationInfo.getValidationData().getWorkflowInstanceId(), SUCCESS, null, validationInfo.getJsonObj(), validationInfo.getJwtPayloadToken());

			logger.info(Constants.App.LOG_TYPE_CONTROL,validationInfo.getValidationData().getWorkflowInstanceId(),String.format("Replace CDA completed for workflow instance id %s", validationInfo.getValidationData().getWorkflowInstanceId()), OperationLogEnum.REPLACE_CDA2, ResultLogEnum.OK, startDateOperation,
					getDocumentType(validationInfo.getDocument()), validationInfo.getJwtPayloadToken());
		} catch (ConnectionRefusedException ce) {
			errorHandlerSRV.connectionRefusedExceptionHandler(startDateOperation, validationInfo.getValidationData(), validationInfo.getJwtPayloadToken(), validationInfo.getJsonObj(), traceInfoDTO, ce, false, getDocumentType(validationInfo.getDocument()));
		} catch (final ValidationException e) {
			errorHandlerSRV.publicationValidationExceptionHandler(startDateOperation, validationInfo.getValidationData(), validationInfo.getJwtPayloadToken(), validationInfo.getJsonObj(), traceInfoDTO, e, false, getDocumentType(validationInfo.getDocument()));
		} 
		String warning = null;

		if (validationInfo.getJsonObj().getMode() == null) {
			warning = Misc.WARN_EXTRACTION_SELECTION;
		}

		log.info("[EXIT] {}() with arguments {}={}, {}={}, {}={}","replace",
				"traceId", traceInfoDTO.getTraceID(),"wif", validationInfo.getValidationData().getWorkflowInstanceId(),"idDoc", idDoc
				);

		return new ResponseEntity<>(new PublicationResDTO(traceInfoDTO, warning, validationInfo.getValidationData().getWorkflowInstanceId()), HttpStatus.OK);
	}


	@Override
	public ResponseWifDTO updateMetadata(final String idDoc, final PublicationMetadataReqDTO requestBody, final HttpServletRequest request) {

		// Estrazione token
		JWTPayloadDTO jwtPayloadToken = null;
		final Date startDateOperation = new Date();
		LogTraceInfoDTO logTraceDTO = getLogTraceInfo();
		String workflowInstanceId = "";

		log.info("[START] {}() with arguments {}={}, {}={}, {}={}","update","traceId", logTraceDTO.getTraceID(),"wif", workflowInstanceId,"idDoc", idDoc);

		String warning = null;
 
		if(!isValidMasterId(idDoc)) throw new ValidationException(createMasterIdError());

		try {
			request.setAttribute("UPDATE_REQ", requestBody);
			jwtPayloadToken = extractAndValidateJWT(request, EventTypeEnum.UPDATE);

			validateUpdateMetadataReq(requestBody);
			workflowInstanceId = createWorkflowInstanceId(idDoc);
			final GetMergedMetadatiDTO metadatiToUpdate = iniClient.metadata(new MergedMetadatiRequestDTO(idDoc,jwtPayloadToken, requestBody));
			if(!StringUtility.isNullOrEmpty(metadatiToUpdate.getErrorMessage()) && !metadatiToUpdate.getErrorMessage().contains("Invalid region ip")) {
				kafkaSRV.sendUpdateStatus(logTraceDTO.getTraceID(), workflowInstanceId, idDoc, BLOCKING_ERROR, jwtPayloadToken, metadatiToUpdate.getErrorMessage(), RIFERIMENTI_INI);
				throw new IniException(metadatiToUpdate.getErrorMessage());
			} else {
				boolean regimeDiMock = metadatiToUpdate.getMarshallResponse()==null; 

				if(regimeDiMock) {
					kafkaSRV.sendUpdateStatus(logTraceDTO.getTraceID(), workflowInstanceId, idDoc, SUCCESS, jwtPayloadToken, "Regime mock", RIFERIMENTI_INI);
				} else {
					kafkaSRV.sendUpdateStatus(logTraceDTO.getTraceID(), workflowInstanceId, idDoc, SUCCESS, jwtPayloadToken, "Merge metadati effettuato correttamente", RIFERIMENTI_INI);
				}
				EdsResponseDTO edsResponse = edsClient.update(new EdsMetadataUpdateReqDTO(idDoc, workflowInstanceId, requestBody));
				if(edsResponse.isEsito()) {
					kafkaSRV.sendUpdateStatus(logTraceDTO.getTraceID(), workflowInstanceId, idDoc, SUCCESS, jwtPayloadToken, "Update EDS effettuato correttamente", EDS_UPDATE);
					if(regimeDiMock) {
						kafkaSRV.sendUpdateStatus(logTraceDTO.getTraceID(), workflowInstanceId, idDoc, SUCCESS, jwtPayloadToken, "Regime di mock",
								INI_UPDATE);
					} else {
						IniTraceResponseDTO res = iniClient.update(new IniMetadataUpdateReqDTO(metadatiToUpdate.getMarshallResponse(), jwtPayloadToken,metadatiToUpdate.getDocumentType(),
								workflowInstanceId));
						// Check response errors
						if(!StringUtility.isNullOrEmpty(res.getErrorMessage())) {
							// Send to indexer
							kafkaSRV.sendUpdateRequest(workflowInstanceId, new IniMetadataUpdateReqDTO(metadatiToUpdate.getMarshallResponse(), jwtPayloadToken, metadatiToUpdate.getDocumentType(),
									workflowInstanceId));
							kafkaSRV.sendUpdateStatus(logTraceDTO.getTraceID(), workflowInstanceId, idDoc, EventStatusEnum.ASYNC_RETRY, jwtPayloadToken, "Transazione presa in carico", INI_UPDATE);
							warning = Misc.WARN_ASYNC_TRANSACTION;
						} else {
							kafkaSRV.sendUpdateStatus(logTraceDTO.getTraceID(), workflowInstanceId, idDoc, SUCCESS, jwtPayloadToken, "Update ini effettuato correttamente", INI_UPDATE);
						}
					}  
				} else {
					kafkaSRV.sendUpdateStatus(logTraceDTO.getTraceID(), workflowInstanceId, idDoc, BLOCKING_ERROR, jwtPayloadToken, "Update EDS fallito", EDS_UPDATE);
					throw new EdsException(edsResponse.getMessageError());
				}

			}

			logger.info(Constants.App.LOG_TYPE_CONTROL,workflowInstanceId,String.format("Update of CDA metadata completed for document with identifier %s", idDoc), OperationLogEnum.UPDATE_METADATA_CDA2, ResultLogEnum.OK, startDateOperation, MISSING_DOC_TYPE_PLACEHOLDER, 
					jwtPayloadToken);
		} catch (MockEnabledException me) {
			throw me;
		} catch (final ValidationException e) {
			errorHandlerSRV.updateValidationExceptionHandler(startDateOperation, logTraceDTO, workflowInstanceId, jwtPayloadToken,e,null, idDoc);
		} catch (Exception e) {
			RestExecutionResultEnum errorInstance = RestExecutionResultEnum.GENERIC_ERROR;
			if (e instanceof ValidationException) {
				errorInstance = get(((ValidationException) e).getError().getType());
			}

			logger.error(Constants.App.LOG_TYPE_CONTROL,workflowInstanceId,String.format("Error while updating CDA metadata of document with identifier %s", idDoc), OperationLogEnum.UPDATE_METADATA_CDA2, ResultLogEnum.KO, startDateOperation, errorInstance.getErrorCategory(), MISSING_DOC_TYPE_PLACEHOLDER,  
					jwtPayloadToken);
			throw e;
		}

		log.info("[EXIT] {}() with arguments {}={}, {}={}, {}={}","update",
				"traceId", logTraceDTO.getTraceID(),"wif", workflowInstanceId,"idDoc", idDoc);
		
		return new ResponseWifDTO(workflowInstanceId, logTraceDTO, warning);
	}

	private ValidationCreationInputDTO publicationAndReplace(final MultipartFile file, final HttpServletRequest request, final boolean isReplace,final String idDoc, final LogTraceInfoDTO traceInfoDTO) {
		ValidationCreationInputDTO validationResult = publicationAndReplaceValidation(file, request, isReplace,idDoc, traceInfoDTO);

		validationResult.setValidationData(executePublicationReplace(validationResult,
				validationResult.getJwtPayloadToken(), validationResult.getJsonObj(), validationResult.getFile(),
				validationResult.getCda()));

		return validationResult;

	}

	private ValidationCreationInputDTO publicationAndReplaceValidation(final MultipartFile file, final HttpServletRequest request, final boolean isReplace,final String idDocRep, final LogTraceInfoDTO traceInfoDTO) {

		final ValidationCreationInputDTO validation = new ValidationCreationInputDTO();
		ValidationDataDTO validationInfo = new ValidationDataDTO();
		validationInfo.setCdaValidated(false);
		validationInfo.setWorkflowInstanceId(MISSING_WORKFLOW_PLACEHOLDER);
		validation.setValidationData(validationInfo);


		try {
			final JWTPayloadDTO jwtPayloadToken = extractAndValidateJWT(request, isReplace ? EventTypeEnum.REPLACE : EventTypeEnum.PUBLICATION);
			validation.setJwtPayloadToken(jwtPayloadToken);

			PublicationCreationReqDTO jsonObj = getAndValidatePublicationReq(request.getParameter("requestBody"), isReplace);
			validation.setJsonObj(jsonObj);

			String idDoc = jsonObj.getIdentificativoDoc();

			if(!isValidMasterId(idDoc)) throw new ValidationException(createMasterIdError());

			final byte[] bytePDF = getAndValidateFile(file);
			validation.setFile(bytePDF);

			if(accreditationSimulationCFG.isEnableCheck()) {
				String idToCheck = StringUtility.isNullOrEmpty(idDocRep) ? idDoc : idDocRep;
				AccreditamentoSimulationDTO simulatedResult = null;
				try {
					simulatedResult = accreditamentoSimulationSRV.runSimulation(idToCheck, bytePDF, isReplace ? EventTypeEnum.REPLACE : EventTypeEnum.PUBLICATION);
				} catch(NoRecordFoundException noRecordFound) {
					kafkaSRV.sendReplaceStatus(traceInfoDTO.getTraceID(), "", EventStatusEnum.BLOCKING_ERROR, "Id documento non presente", jsonObj, jwtPayloadToken);
					throw noRecordFound;
				}
				
				if(simulatedResult!=null) {
					jsonObj.setWorkflowInstanceId(simulatedResult.getWorkflowInstanceId());
				}
			}

			final String cda = extractCDA(bytePDF, jsonObj.getMode());
			validation.setCda(cda);

			validateJWT(validation.getJwtPayloadToken(), cda);

			final org.jsoup.nodes.Document docT = Jsoup.parse(cda);
			final String key = extractFieldCda(docT);
			validation.setDocument(docT);
			validation.setKafkaKey(key);
		} catch (final ValidationException | NoRecordFoundException ve) {
			cdaSRV.consumeHash(validationInfo.getHash());
//			validation.setValidationError(ve);
			throw ve;
		}  

		return validation;
	}

	private ValidationDataDTO executePublicationReplace(final ValidationCreationInputDTO validation,
			final JWTPayloadDTO jwtPayloadToken, PublicationCreationReqDTO jsonObj, final byte[] bytePDF,
			final String cda) {
		ValidationDataDTO validationInfo;
		validationInfo = getValidationInfo(cda, jsonObj.getWorkflowInstanceId());
		validation.setValidationData(validationInfo); // Updating validation info

		ValidationDataDTO validatedDocument = cdaSRV.getByWorkflowInstanceId(validationInfo.getWorkflowInstanceId()); 

		cdaSRV.consumeHash(validationInfo.getHash()); 

		ValidationUtility.checkDayAfterValidation(validatedDocument.getInsertionDate(), validationCFG.getDaysAllowToPublishAfterValidation());

		final String documentSha256 = encodeSHA256(bytePDF);
		validation.setDocumentSha(documentSha256);

		validateDocumentHash(documentSha256, validation.getJwtPayloadToken());

		ResourceDTO fhirMappingResult = callFhirMappingEngine(validatedDocument.getTransformID(), validatedDocument.getEngineID(), jwtPayloadToken, jsonObj, bytePDF, cda,documentSha256);
		validation.setFhirResource(fhirMappingResult);
		return validationInfo;
	}
	

	private ResourceDTO callFhirMappingEngine(String transformId, String engineId,
			final JWTPayloadDTO jwtPayloadToken, PublicationCreationReqDTO jsonObj, final byte[] bytePDF,
			final String cda, final String documentSha256) {
		final ResourceDTO fhirResourcesDTO = documentReferenceSRV.createFhirResources(cda, jsonObj, bytePDF.length, documentSha256, jwtPayloadToken.getPerson_id(), transformId, engineId);

		if(!isNullOrEmpty(fhirResourcesDTO.getErrorMessage())) {
			final ErrorResponseDTO error = ErrorResponseDTO.builder()
					.type(FHIR_MAPPING_ERROR.getType())
					.title(FHIR_MAPPING_ERROR.getTitle())
					.instance(ErrorInstanceEnum.FHIR_RESOURCE_ERROR.getInstance())
					.detail(fhirResourcesDTO.getErrorMessage()).build();

			throw new ValidationException(error);
		}

		return fhirResourcesDTO;
	}

	@Override
	public ResponseWifDTO delete(String idDoc, HttpServletRequest request) {
		final Date startOperation = new Date();
		// Create request tracking
		LogTraceInfoDTO info = getLogTraceInfo();
		String workflowInstanceId = createWorkflowInstanceId(idDoc);

		log.info("[START] {}() with arguments {}={}, {}={}, {}={}","delete","traceId", info.getTraceID(),"wif", workflowInstanceId,"idDoc", idDoc);

		JWTPayloadDTO jwtPayloadToken = null;
		String warning = null;

		String subjApplicationId = null;
		String subjApplicationVendor = null;
		String subjApplicationVersion = null;

		if(!isValidMasterId(idDoc)) throw new ValidationException(createMasterIdError());

		try {
			// Extract token
			jwtPayloadToken = extractAndValidateJWT(request, EventTypeEnum.DELETE); 

			subjApplicationId = jwtPayloadToken.getSubject_application_id(); 
			subjApplicationVendor = jwtPayloadToken.getSubject_application_vendor();
			subjApplicationVersion = jwtPayloadToken.getSubject_application_version();

			// ==============================
			// [1] Retrieve reference from INI
			// ==============================
			IniReferenceResponseDTO iniReference = iniClient.reference(new IniReferenceRequestDTO(idDoc, jwtPayloadToken));
			// Exit if necessary
			if(!isNullOrEmpty(iniReference.getErrorMessage())) {
				kafkaSRV.sendDeleteStatus(info.getTraceID(), workflowInstanceId, idDoc, iniReference.getErrorMessage(), BLOCKING_ERROR, jwtPayloadToken, RIFERIMENTI_INI);
				throw new IniException(iniReference.getErrorMessage());	
			} else {
				kafkaSRV.sendDeleteStatus(info.getTraceID(), workflowInstanceId, idDoc, "Riferimenti trovati: " +iniReference.getUuid(), SUCCESS, jwtPayloadToken, RIFERIMENTI_INI);
			}

			// ==============================
			// [2] Send delete request to EDS
			// ==============================
			EdsResponseDTO edsResponse = edsClient.delete(idDoc);
			// Exit if necessary
			Objects.requireNonNull(edsResponse, "PublicationCTL returned an error - edsResponse is null!");

			if (!edsResponse.isEsito()) {
				// Update transaction status
				kafkaSRV.sendDeleteStatus(info.getTraceID(), workflowInstanceId, idDoc, edsResponse.getMessageError(), BLOCKING_ERROR, jwtPayloadToken, EDS_DELETE);
				throw new EdsException("Error encountered while sending delete information to EDS client");
			} else {
				// Update transaction status
				kafkaSRV.sendDeleteStatus(info.getTraceID(), workflowInstanceId, idDoc, "Delete effettuata su eds", SUCCESS, jwtPayloadToken, EDS_DELETE);
			}


			// ==============================
			// [3] Send delete request to INI
			// ==============================
			DeleteRequestDTO deleteRequestDTO = buildRequestForIni(idDoc, iniReference.getUuid(), jwtPayloadToken,iniReference.getDocumentType(),
					subjApplicationId, subjApplicationVendor, subjApplicationVersion,workflowInstanceId);
			IniTraceResponseDTO iniResponse = iniClient.delete(deleteRequestDTO);

			// Check mock errors
			boolean iniMockMessage = !isNullOrEmpty(iniResponse.getErrorMessage()) && iniResponse.getErrorMessage().contains("Invalid region ip");
			// Exit if necessary
			if (iniMockMessage) {
				throw new MockEnabledException(iniResponse.getErrorMessage(), edsResponse.getMessageError());
			}

			// Check response errors
			if(!isNullOrEmpty(iniResponse.getErrorMessage())) {
				// Send to indexer
				kafkaSRV.sendDeleteRequest(workflowInstanceId, deleteRequestDTO);
				// Update transaction status
				kafkaSRV.sendDeleteStatus(info.getTraceID(), workflowInstanceId, idDoc, "Transazione presa in carico", EventStatusEnum.ASYNC_RETRY, jwtPayloadToken,
						INI_DELETE);
				warning = Misc.WARN_ASYNC_TRANSACTION;
			} else {
				// Update transaction status
				kafkaSRV.sendDeleteStatus(info.getTraceID(), workflowInstanceId, idDoc, "Delete effettuata su ini", SUCCESS, jwtPayloadToken, INI_DELETE);
			}

			logger.info(Constants.App.LOG_TYPE_CONTROL,workflowInstanceId,String.format("Deletion of CDA completed for document with identifier %s", idDoc), OperationLogEnum.DELETE_CDA2, ResultLogEnum.OK, startOperation, MISSING_DOC_TYPE_PLACEHOLDER, 
					jwtPayloadToken);
		} catch(MockEnabledException me) {
			throw me;
		} catch(IniException inEx) {
			logger.error(Constants.App.LOG_TYPE_CONTROL,workflowInstanceId,String.format("Error while delete record from ini %s", idDoc), OperationLogEnum.DELETE_CDA2, ResultLogEnum.KO, startOperation, INI_EXCEPTION.getErrorCategory(), MISSING_DOC_TYPE_PLACEHOLDER, 
					jwtPayloadToken);
			throw inEx;

		} catch (Exception e) {
			RestExecutionResultEnum errorInstance = RestExecutionResultEnum.GENERIC_ERROR;
			if (e instanceof ValidationException) {
				errorInstance = get(((ValidationException) e).getError().getType());
			}

			logger.error(Constants.App.LOG_TYPE_CONTROL,workflowInstanceId,String.format("Error while deleting CDA of document with identifier %s", idDoc), OperationLogEnum.DELETE_CDA2, ResultLogEnum.KO, startOperation, errorInstance.getErrorCategory(), MISSING_DOC_TYPE_PLACEHOLDER, 
					jwtPayloadToken);
			throw e;
		}

		log.info("[EXIT] {}() with arguments {}={}, {}={}, {}={}",
				"delete",
				"traceId", info.getTraceID(),
				"wif", workflowInstanceId,
				"idDoc", idDoc
				);


		return new ResponseWifDTO(workflowInstanceId, info, warning);
	}

	private DeleteRequestDTO buildRequestForIni(final String identificativoDocumento, final String uuid, final JWTPayloadDTO jwtPayloadToken,
			final String documentType, String applicationId, String applicationVendor, String applicationVersion,
			final String workflowInstanceId) {
		DeleteRequestDTO out = null;
		try {
			out = DeleteRequestDTO.builder().
					action_id(jwtPayloadToken.getAction_id()).
					idDoc(identificativoDocumento).
					uuid(uuid).
					iss(jwtPayloadToken.getIss()).
					locality(jwtPayloadToken.getLocality()).
					patient_consent(jwtPayloadToken.getPatient_consent()).
					person_id(jwtPayloadToken.getPerson_id()).
					purpose_of_use(jwtPayloadToken.getPurpose_of_use()).
					resource_hl7_type(jwtPayloadToken.getResource_hl7_type()).
					sub(jwtPayloadToken.getSub()).
					subject_organization_id(jwtPayloadToken.getSubject_organization_id()).
					subject_organization(jwtPayloadToken.getSubject_organization()).
					subject_role(jwtPayloadToken.getSubject_role()).
					documentType(documentType).
					subject_application_id(applicationId).
					subject_application_vendor(applicationVendor).
					subject_application_version(applicationVersion).
					workflow_instance_id(workflowInstanceId).
					build();
		} catch(Exception ex) {
			log.error("Error while build request delete for ini : " , ex);
			throw new BusinessException("Error while build request delete for ini : " , ex);
		}
		return out;
	}

	@Override
	public ResponseEntity<PublicationResDTO> validateAndCreate(PublicationFatherCreationReqDTO requestBody,MultipartFile file, HttpServletRequest request) {
		final Date startDateOperationValidation = new Date();
		LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();

		String workflowInstanceId = Constants.App.MISSING_WORKFLOW_PLACEHOLDER;
		String warning = null;
		Document docT = null;
		ValidationCreationInputDTO validationResult = new ValidationCreationInputDTO();
		try {
			//Valido request e jwt come se fosse una pubblicazione
			validationResult = publicationAndReplaceValidation(file, request, false,null, traceInfoDTO);

			docT = Jsoup.parse(validationResult.getCda());
			workflowInstanceId = CdaUtility.getWorkflowInstanceId(docT);

			//Chiamo ms validator per la validazione
			warning = validate(validationResult.getCda(), ActivityEnum.VALIDATION, workflowInstanceId);

			kafkaSRV.sendValidationStatus(traceInfoDTO.getTraceID(), workflowInstanceId, EventStatusEnum.SUCCESS,null, validationResult.getJwtPayloadToken(),
					EventTypeEnum.VALIDATION_FOR_PUBLICATION);

			logger.info(Constants.App.LOG_TYPE_CONTROL,workflowInstanceId, "Validation CDA completed for workflow instance Id " + workflowInstanceId, OperationLogEnum.VAL_CDA2, ResultLogEnum.OK, startDateOperationValidation, CdaUtility.getDocumentType(docT),validationResult.getJwtPayloadToken());
			request.setAttribute("JWT_ISSUER", validationResult.getJwtPayloadToken().getIss());
		} catch (final ValidationException e) {
			errorHandlerSRV.validationExceptionHandler(startDateOperationValidation, traceInfoDTO, workflowInstanceId, validationResult.getJwtPayloadToken(), e, CdaUtility.getDocumentType(docT));
		}
		
		final Date startDateOperationPublication = new Date();
		try {
			//Eseguo le operazione di creazione
			ValidationDataDTO dto = executePublicationReplace(validationResult,
					validationResult.getJwtPayloadToken(), validationResult.getJsonObj(), validationResult.getFile(),
					validationResult.getCda());
			validationResult.setValidationData(dto);

			//Eseguo le operazione post creazione
			postExecutionCreate(startDateOperationPublication, traceInfoDTO, validationResult);
		} catch (ConnectionRefusedException ce) {		
			errorHandlerSRV.connectionRefusedExceptionHandler(startDateOperationPublication, validationResult.getValidationData(), validationResult.getJwtPayloadToken(), validationResult.getJsonObj(), traceInfoDTO, ce, true, getDocumentType(validationResult.getDocument()));
		} catch (final ValidationException e) {
			errorHandlerSRV.publicationValidationExceptionHandler(startDateOperationPublication, validationResult.getValidationData(), validationResult.getJwtPayloadToken(), validationResult.getJsonObj(), traceInfoDTO, e, true, getDocumentType(validationResult.getDocument()));
		}

		warning = StringUtility.isNullOrEmpty(warning) ? null : warning; 
		return new ResponseEntity<>(new PublicationResDTO(traceInfoDTO, warning, validationResult.getValidationData().getWorkflowInstanceId()), HttpStatus.CREATED);
	}
	
	 
	@Override
	public ResponseEntity<PublicationResDTO> validateAndReplace(@Size(min = 1, max = 256) String idDoc,
			PublicationUpdateReqDTO requestBody, MultipartFile file, HttpServletRequest request) {
		final Date startDateValidationOperation = new Date();
		LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();

		String workflowInstanceId = Constants.App.MISSING_WORKFLOW_PLACEHOLDER;
		String warning = null;
		Document docT = null;
		ValidationCreationInputDTO validationResult = new ValidationCreationInputDTO();
		try {
			//Valido request e jwt come se fosse una pubblicazione
			validationResult = publicationAndReplaceValidation(file, request, false,idDoc,traceInfoDTO);

			docT = Jsoup.parse(validationResult.getCda());
			workflowInstanceId = CdaUtility.getWorkflowInstanceId(docT);

			//Chiamo ms validator per la validazione
			warning = validate(validationResult.getCda(), ActivityEnum.VALIDATION, workflowInstanceId);

			kafkaSRV.sendValidationStatus(traceInfoDTO.getTraceID(), workflowInstanceId, EventStatusEnum.SUCCESS,null, validationResult.getJwtPayloadToken(),
					EventTypeEnum.VALIDATION_FOR_REPLACE);

			logger.info(Constants.App.LOG_TYPE_CONTROL,workflowInstanceId, "Validation CDA completed for workflow instance Id " + workflowInstanceId, OperationLogEnum.VAL_CDA2, ResultLogEnum.OK, startDateValidationOperation, CdaUtility.getDocumentType(docT),validationResult.getJwtPayloadToken());
			request.setAttribute("JWT_ISSUER", validationResult.getJwtPayloadToken().getIss());
		} catch (final ValidationException e) {
			errorHandlerSRV.validationExceptionHandler(startDateValidationOperation, traceInfoDTO, workflowInstanceId, validationResult.getJwtPayloadToken(), e, CdaUtility.getDocumentType(docT));
		}
		final Date startDateReplacenOperation = new Date();
		try {
			//Eseguo le operazione di creazione
			ValidationDataDTO dto = executePublicationReplace(validationResult,
					validationResult.getJwtPayloadToken(), validationResult.getJsonObj(), validationResult.getFile(),
					validationResult.getCda());
			validationResult.setValidationData(dto);
			log.info("[START] {}() with arguments {}={}, {}={}, {}={}","replace","traceId", traceInfoDTO.getTraceID(),"wif", validationResult.getValidationData().getWorkflowInstanceId(),"idDoc", idDoc);

			IniReferenceRequestDTO iniReq = new IniReferenceRequestDTO(idDoc, validationResult.getJwtPayloadToken());
			IniReferenceResponseDTO response = iniClient.reference(iniReq);

			if(!isNullOrEmpty(response.getErrorMessage())) {
				log.error("Errore. Nessun riferimento trovato.");
				throw new IniException(response.getErrorMessage());
			}


			log.debug("Executing replace of document: {}", idDoc);
			iniInvocationSRV.replace(validationResult.getValidationData().getWorkflowInstanceId(), validationResult.getFhirResource(), validationResult.getJwtPayloadToken(), response.getUuid());

			final IndexerValueDTO kafkaValue = new IndexerValueDTO();
			kafkaValue.setWorkflowInstanceId(validationResult.getValidationData().getWorkflowInstanceId());
			kafkaValue.setIdDoc(idDoc);
			kafkaValue.setEdsDPOperation(ProcessorOperationEnum.REPLACE);

			kafkaSRV.notifyChannel(validationResult.getKafkaKey(), new Gson().toJson(kafkaValue), PriorityTypeEnum.LOW, validationResult.getJsonObj().getTipoDocumentoLivAlto(), DestinationTypeEnum.INDEXER);
			kafkaSRV.sendReplaceStatus(traceInfoDTO.getTraceID(), validationResult.getValidationData().getWorkflowInstanceId(), SUCCESS, null, validationResult.getJsonObj(), validationResult.getJwtPayloadToken());

			logger.info(Constants.App.LOG_TYPE_CONTROL,validationResult.getValidationData().getWorkflowInstanceId(),String.format("Replace CDA completed for workflow instance id %s", validationResult.getValidationData().getWorkflowInstanceId()), OperationLogEnum.REPLACE_CDA2, ResultLogEnum.OK, startDateReplacenOperation,
					getDocumentType(validationResult.getDocument()), validationResult.getJwtPayloadToken());
		} catch (ConnectionRefusedException ce) {		
			errorHandlerSRV.connectionRefusedExceptionHandler(startDateReplacenOperation, validationResult.getValidationData(), validationResult.getJwtPayloadToken(), validationResult.getJsonObj(), traceInfoDTO, ce, true, getDocumentType(validationResult.getDocument()));
		} catch (final ValidationException e) {
			errorHandlerSRV.publicationValidationExceptionHandler(startDateReplacenOperation, validationResult.getValidationData(), validationResult.getJwtPayloadToken(), validationResult.getJsonObj(), traceInfoDTO, e, true, getDocumentType(validationResult.getDocument()));
		}
		
		return new ResponseEntity<>(new PublicationResDTO(traceInfoDTO, warning, validationResult.getValidationData().getWorkflowInstanceId()), HttpStatus.OK);
	
	}

	@Override
	public ResponseEntity<ConversionResDTO> convert(PublicationFatherCreationReqDTO requestBody,MultipartFile file, HttpServletRequest request){
		final Date startDateOperationValidation = new Date();
		LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();

		String workflowInstanceId = Constants.App.MISSING_WORKFLOW_PLACEHOLDER;
		String warning = null;
		Document docT = null;
		ValidationCreationInputDTO validationResult = new ValidationCreationInputDTO();
		try {
			//Valido request e jwt come se fosse una pubblicazione
			validationResult = publicationAndReplaceValidation(file, request, false,null, traceInfoDTO);

			docT = Jsoup.parse(validationResult.getCda());
			workflowInstanceId = CdaUtility.getWorkflowInstanceId(docT);

			//Chiamo ms validator per la validazione
			warning = validate(validationResult.getCda(), ActivityEnum.VALIDATION, workflowInstanceId);

			kafkaSRV.sendValidationStatus(traceInfoDTO.getTraceID(), workflowInstanceId, EventStatusEnum.SUCCESS,null, validationResult.getJwtPayloadToken(),
					EventTypeEnum.VALIDATION_FOR_PUBLICATION);

			logger.info(Constants.App.LOG_TYPE_CONTROL,workflowInstanceId, "Validation CDA completed for workflow instance Id " + workflowInstanceId, OperationLogEnum.VAL_CDA2, ResultLogEnum.OK, startDateOperationValidation, CdaUtility.getDocumentType(docT),validationResult.getJwtPayloadToken());
			request.setAttribute("JWT_ISSUER", validationResult.getJwtPayloadToken().getIss());
		} catch (final ValidationException e) {
			errorHandlerSRV.validationExceptionHandler(startDateOperationValidation, traceInfoDTO, workflowInstanceId, validationResult.getJwtPayloadToken(), e, CdaUtility.getDocumentType(docT));
		}
		final Date startDateOperationConversion = new Date();
		try {
			//Eseguo le operazione di creazione
			ValidationDataDTO dto = executePublicationReplace(validationResult,
					validationResult.getJwtPayloadToken(), validationResult.getJsonObj(), validationResult.getFile(),
					validationResult.getCda());
		} catch (ConnectionRefusedException ce) {		
			errorHandlerSRV.connectionRefusedExceptionHandler(startDateOperationConversion, validationResult.getValidationData(), validationResult.getJwtPayloadToken(), validationResult.getJsonObj(), traceInfoDTO, ce, true, getDocumentType(validationResult.getDocument()));
		} catch (final ValidationException e) {
			errorHandlerSRV.publicationValidationExceptionHandler(startDateOperationConversion	, validationResult.getValidationData(), validationResult.getJwtPayloadToken(), validationResult.getJsonObj(), traceInfoDTO, e, true, getDocumentType(validationResult.getDocument()));
		}

		warning = StringUtility.isNullOrEmpty(warning) ? null : warning; 
		return new ResponseEntity<>(new ConversionResDTO(traceInfoDTO,workflowInstanceId, warning, validationResult.getFhirResource().getBundleJson()), HttpStatus.OK);
	}
}
