/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import static it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants.App.JWT_MISSING_ISSUER_PLACEHOLDER;
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
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum.OLDER_DAY;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum.get;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility.createWorkflowInstanceId;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility.extractFieldCda;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility.getDocumentType;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility.encodeSHA256;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility.isNullOrEmpty;

import java.util.Date;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IIniClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants.Headers;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants.Misc;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.ValidationCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.IPublicationCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationCreationInputDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.DeleteRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.IniMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.IniReferenceRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.MergedMetadatiRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationMetadataReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.GetMergedMetadatiDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniReferenceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniTraceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.PublicationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ResponseWifDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.DestinationTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ErrorInstanceEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;
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
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.logging.LoggerHelper;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDocumentReferenceSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IErrorHandlerSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.facade.ICdaFacadeSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl.IniEdsInvocationSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CfUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.DateUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
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
 
	
	@Override
	public ResponseEntity<PublicationResDTO> create(final PublicationCreationReqDTO requestBody, final MultipartFile file, final HttpServletRequest request) {
		final Date startDateOperation = new Date();
		final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();

		ValidationCreationInputDTO validationInfo = new ValidationCreationInputDTO();
		validationInfo.setValidationData(new ValidationDataDTO(null, false, MISSING_WORKFLOW_PLACEHOLDER, null, new Date(),false));

		String role = Constants.App.JWT_MISSING_SUBJECT_ROLE;
		String subjectFiscalCode = Constants.App.JWT_MISSING_SUBJECT;
		String locality = Constants.App.JWT_MISSING_LOCALITY;
		
		String subjApplicationId = null;
		String subjApplicationVendor = null;
		String subjApplicationVersion = null;
		try {
			validationInfo = validateInput(file, request, false,traceInfoDTO);

			if (validationInfo.getValidationError() != null) {
				throw validationInfo.getValidationError();
			}

			subjApplicationId = validationInfo.getJwtToken().getPayload().getSubject_application_id(); 
			subjApplicationVendor = validationInfo.getJwtToken().getPayload().getSubject_application_vendor();
			subjApplicationVersion = validationInfo.getJwtToken().getPayload().getSubject_application_version();

			iniInvocationSRV.insert(validationInfo.getValidationData().getWorkflowInstanceId(), validationInfo.getFhirResource(), validationInfo.getJwtToken());
			
			PriorityTypeEnum priorityType = PriorityTypeEnum.NULL;
			if (validationInfo.getJsonObj().getPriorita() != null) {
				priorityType = Boolean.TRUE.equals(validationInfo.getJsonObj().getPriorita()) ? PriorityTypeEnum.HIGH : PriorityTypeEnum.LOW;
			}

			final IndexerValueDTO kafkaValue = new IndexerValueDTO();
			kafkaValue.setWorkflowInstanceId(validationInfo.getValidationData().getWorkflowInstanceId());
			kafkaValue.setIdDoc(validationInfo.getJsonObj().getIdentificativoDoc());
			kafkaValue.setEdsDPOperation(ProcessorOperationEnum.PUBLISH);

			kafkaSRV.notifyChannel(validationInfo.getKafkaKey(), new Gson().toJson(kafkaValue), priorityType, validationInfo.getJsonObj().getTipoDocumentoLivAlto(), DestinationTypeEnum.INDEXER);
			kafkaSRV.sendPublicationStatus(traceInfoDTO.getTraceID(), validationInfo.getValidationData().getWorkflowInstanceId(), SUCCESS, null, validationInfo.getJsonObj(), validationInfo.getJwtToken() != null ? validationInfo.getJwtToken().getPayload() : null);
			
			role = validationInfo.getJwtToken().getPayload().getSubject_role();
			locality = validationInfo.getJwtToken().getPayload().getLocality();
			subjectFiscalCode = CfUtility.extractFiscalCodeFromJwtSub(validationInfo.getJwtToken().getPayload().getSub());
			logger.info(String.format("Publication CDA completed for workflow instance id %s", validationInfo.getValidationData().getWorkflowInstanceId()), OperationLogEnum.PUB_CDA2, ResultLogEnum.OK, startDateOperation, validationInfo.getJwtToken().getPayload().getIss(), getDocumentType(validationInfo.getDocument()), role, subjectFiscalCode, locality,
					subjApplicationId, subjApplicationVendor,subjApplicationVersion);
		} catch (ConnectionRefusedException ce) {
			errorHandlerSRV.connectionRefusedExceptionHandler(startDateOperation, validationInfo.getValidationData(), validationInfo.getJwtToken(), validationInfo.getJsonObj(), traceInfoDTO, ce, true, getDocumentType(validationInfo.getDocument()));
		} catch (final ValidationException e) {
			errorHandlerSRV.publicationValidationExceptionHandler(startDateOperation, validationInfo.getValidationData(), validationInfo.getJwtToken(), validationInfo.getJsonObj(), traceInfoDTO, e, true, getDocumentType(validationInfo.getDocument()));
		}

		String warning = null;
		
		if (validationInfo.getJsonObj().getMode() == null) {
			warning = Misc.WARN_EXTRACTION_SELECTION;
		}
		
		return new ResponseEntity<>(new PublicationResDTO(traceInfoDTO, warning, validationInfo.getValidationData().getWorkflowInstanceId()), HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<PublicationResDTO> replace(final String idDoc, final PublicationUpdateReqDTO requestBody, final MultipartFile file, final HttpServletRequest request) {
		
			final Date startDateOperation = new Date();
			final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();

			ValidationCreationInputDTO validationInfo = new ValidationCreationInputDTO();
			validationInfo.setValidationData(new ValidationDataDTO(null, false, MISSING_WORKFLOW_PLACEHOLDER, null, new Date(),false));

			String role = Constants.App.JWT_MISSING_SUBJECT_ROLE;
			String subjectFiscalCode = Constants.App.JWT_MISSING_SUBJECT;
			String locality = Constants.App.JWT_MISSING_LOCALITY;

			String subjApplicationId = null;
			String subjApplicationVendor = null;
			String subjApplicationVersion = null;
			
			try {
				validationInfo = validateInput(file, request, true,traceInfoDTO);

				if (validationInfo.getValidationError() != null) {
					throw validationInfo.getValidationError();
				}
				
				subjApplicationId = validationInfo.getJwtToken().getPayload().getSubject_application_id(); 
				subjApplicationVendor = validationInfo.getJwtToken().getPayload().getSubject_application_vendor();
				subjApplicationVersion = validationInfo.getJwtToken().getPayload().getSubject_application_version();
				
				IniReferenceRequestDTO iniReq = new IniReferenceRequestDTO(idDoc, validationInfo.getJwtToken().getPayload());
				IniReferenceResponseDTO response = iniClient.reference(iniReq);
				
				if(!isNullOrEmpty(response.getErrorMessage())) {
					log.error("Errore. Nessun riferimento trovato.");
					throw new IniException(response.getErrorMessage());
				}
				

				log.debug("Executing replace of document: {}", idDoc);
				iniInvocationSRV.replace(validationInfo.getValidationData().getWorkflowInstanceId(), validationInfo.getFhirResource(), validationInfo.getJwtToken(), response.getUuid());
				
				final IndexerValueDTO kafkaValue = new IndexerValueDTO();
				kafkaValue.setWorkflowInstanceId(validationInfo.getValidationData().getWorkflowInstanceId());
				kafkaValue.setIdDoc(idDoc);
				kafkaValue.setEdsDPOperation(ProcessorOperationEnum.REPLACE);
				
				kafkaSRV.notifyChannel(validationInfo.getKafkaKey(), new Gson().toJson(kafkaValue), PriorityTypeEnum.LOW, validationInfo.getJsonObj().getTipoDocumentoLivAlto(), DestinationTypeEnum.INDEXER);
				kafkaSRV.sendReplaceStatus(traceInfoDTO.getTraceID(), validationInfo.getValidationData().getWorkflowInstanceId(), SUCCESS, null, validationInfo.getJsonObj(), validationInfo.getJwtToken() != null ? validationInfo.getJwtToken().getPayload() : null);

				role = validationInfo.getJwtToken().getPayload().getSubject_role();
				locality = validationInfo.getJwtToken().getPayload().getLocality();
				subjectFiscalCode = CfUtility.extractFiscalCodeFromJwtSub(validationInfo.getJwtToken().getPayload().getSub());

				logger.info(String.format("Replace CDA completed for workflow instance id %s", validationInfo.getValidationData().getWorkflowInstanceId()), OperationLogEnum.REPLACE_CDA2, ResultLogEnum.OK, startDateOperation, validationInfo.getJwtToken().getPayload().getIss(), getDocumentType(validationInfo.getDocument()), role, subjectFiscalCode, locality,
						subjApplicationId, subjApplicationVendor, subjApplicationVersion);
			} catch (ConnectionRefusedException ce) {
				errorHandlerSRV.connectionRefusedExceptionHandler(startDateOperation, validationInfo.getValidationData(), validationInfo.getJwtToken(), validationInfo.getJsonObj(), traceInfoDTO, ce, false, getDocumentType(validationInfo.getDocument()));
			} catch (final ValidationException e) {
				errorHandlerSRV.publicationValidationExceptionHandler(startDateOperation, validationInfo.getValidationData(), validationInfo.getJwtToken(), validationInfo.getJsonObj(), traceInfoDTO, e, false, getDocumentType(validationInfo.getDocument()));
			}
	
			String warning = null;
			
			if (validationInfo.getJsonObj().getMode() == null) {
				warning = Misc.WARN_EXTRACTION_SELECTION;
			}
			
			return new ResponseEntity<>(new PublicationResDTO(traceInfoDTO, warning, validationInfo.getValidationData().getWorkflowInstanceId()), HttpStatus.OK);
	}


	@Override
	public ResponseWifDTO updateMetadata(final String idDoc, final PublicationMetadataReqDTO jsonObj, final HttpServletRequest request) {

		// Estrazione token
		JWTTokenDTO jwtToken = null;
		final Date startDateOperation = new Date();
		String workflowInstanceId = createWorkflowInstanceId(idDoc);

		String role = Constants.App.JWT_MISSING_SUBJECT_ROLE;
		String subjectFiscalCode = Constants.App.JWT_MISSING_SUBJECT;
		String locality = Constants.App.JWT_MISSING_LOCALITY;
		String warning = null;

		LogTraceInfoDTO logTraceDTO = null;
		
		String subjApplicationId = null;
		String subjApplicationVendor = null;
		String subjApplicationVersion = null;
		try {
			logTraceDTO = getLogTraceInfo(); 
			if (Boolean.TRUE.equals(msCfg.getFromGovway())) {
				jwtToken = extractAndValidateJWT(request.getHeader(Headers.JWT_GOVWAY_HEADER), msCfg.getFromGovway());
			} else {
				jwtToken = extractAndValidateJWT(request.getHeader(Headers.JWT_HEADER), msCfg.getFromGovway());
			}

			role = jwtToken.getPayload().getSubject_role();
			locality = jwtToken.getPayload().getLocality();
			subjectFiscalCode = CfUtility.extractFiscalCodeFromJwtSub(jwtToken.getPayload().getSub());

			validateUpdateMetadataReq(jsonObj);
			
			subjApplicationId = jwtToken.getPayload().getSubject_application_id(); 
			subjApplicationVendor = jwtToken.getPayload().getSubject_application_vendor();
			subjApplicationVersion = jwtToken.getPayload().getSubject_application_version(); 

			final GetMergedMetadatiDTO metadatiToUpdate = iniClient.metadata(new MergedMetadatiRequestDTO(idDoc,jwtToken.getPayload(), jsonObj));
			if(!StringUtility.isNullOrEmpty(metadatiToUpdate.getErrorMessage()) && !metadatiToUpdate.getErrorMessage().contains("Invalid region ip")) {
				kafkaSRV.sendUpdateStatus(logTraceDTO.getTraceID(), workflowInstanceId, idDoc, BLOCKING_ERROR, jwtToken.getPayload(), metadatiToUpdate.getErrorMessage(), RIFERIMENTI_INI);
				throw new IniException(metadatiToUpdate.getErrorMessage());
			} else {
				boolean regimeDiMock = metadatiToUpdate!=null && metadatiToUpdate.getMarshallResponse()==null; 
				
				if(regimeDiMock) {
					kafkaSRV.sendUpdateStatus(logTraceDTO.getTraceID(), workflowInstanceId, idDoc, SUCCESS, jwtToken.getPayload(), "Regime mock", RIFERIMENTI_INI);
				} else {
					kafkaSRV.sendUpdateStatus(logTraceDTO.getTraceID(), workflowInstanceId, idDoc, SUCCESS, jwtToken.getPayload(), "Merge metadati effettuato correttamente", RIFERIMENTI_INI);
				}
				EdsResponseDTO edsResponse = edsClient.update(new EdsMetadataUpdateReqDTO(idDoc, workflowInstanceId, jsonObj));
				if(edsResponse.isEsito()) {
					kafkaSRV.sendUpdateStatus(logTraceDTO.getTraceID(), workflowInstanceId, idDoc, SUCCESS, jwtToken.getPayload(), "Update EDS effettuato correttamente", EDS_UPDATE);
					if(regimeDiMock) {
						kafkaSRV.sendUpdateStatus(logTraceDTO.getTraceID(), workflowInstanceId, idDoc, SUCCESS, jwtToken.getPayload(), "Regime di mock",
								INI_UPDATE);
					} else {
						IniTraceResponseDTO res = iniClient.update(new IniMetadataUpdateReqDTO(metadatiToUpdate.getMarshallResponse(), jwtToken.getPayload(),metadatiToUpdate.getDocumentType()));
						// Check response errors
						if(!StringUtility.isNullOrEmpty(res.getErrorMessage())) {
							// Send to indexer
							kafkaSRV.sendUpdateRequest(workflowInstanceId, new IniMetadataUpdateReqDTO(metadatiToUpdate.getMarshallResponse(), jwtToken.getPayload(), metadatiToUpdate.getDocumentType()));
							kafkaSRV.sendUpdateStatus(logTraceDTO.getTraceID(), workflowInstanceId, idDoc, EventStatusEnum.ASYNC_RETRY, jwtToken.getPayload(), "Transazione presa in carico", INI_UPDATE);
							warning = Misc.WARN_ASYNC_TRANSACTION;
						} else {
							kafkaSRV.sendUpdateStatus(logTraceDTO.getTraceID(), workflowInstanceId, idDoc, SUCCESS, jwtToken.getPayload(), "Update ini effettuato correttamente", INI_UPDATE);
						}
					}  
				} else {
					kafkaSRV.sendUpdateStatus(logTraceDTO.getTraceID(), workflowInstanceId, idDoc, BLOCKING_ERROR, jwtToken.getPayload(), "Update EDS fallito", EDS_UPDATE);
					throw new EdsException(edsResponse.getMessageError());
				}

			}

			logger.info(String.format("Update of CDA metadata completed for document with identifier %s", idDoc), OperationLogEnum.UPDATE_METADATA_CDA2, ResultLogEnum.OK, startDateOperation, jwtToken.getPayload().getIss(), MISSING_DOC_TYPE_PLACEHOLDER, role, subjectFiscalCode, locality,
					subjApplicationId,subjApplicationVendor,subjApplicationVersion);
		} catch (MockEnabledException me) {
			throw me;
		} catch (Exception e) {
			final String issuer = jwtToken != null ? jwtToken.getPayload().getIss() : JWT_MISSING_ISSUER_PLACEHOLDER;
			RestExecutionResultEnum errorInstance = RestExecutionResultEnum.GENERIC_ERROR;
			if (e instanceof ValidationException) {
				errorInstance = get(((ValidationException) e).getError().getType());
			}

			logger.error(String.format("Error while updating CDA metadata of document with identifier %s", idDoc), OperationLogEnum.UPDATE_METADATA_CDA2, ResultLogEnum.KO, startDateOperation, errorInstance.getErrorCategory(), issuer, MISSING_DOC_TYPE_PLACEHOLDER, role, subjectFiscalCode, locality,
					subjApplicationId,subjApplicationVendor,subjApplicationVersion);
			throw e;
		}

		return new ResponseWifDTO(workflowInstanceId, logTraceDTO, warning);
	}

	private ValidationCreationInputDTO validateInput(final MultipartFile file, final HttpServletRequest request, final boolean isReplace, final LogTraceInfoDTO traceInfoDTO) {

		final ValidationCreationInputDTO validation = new ValidationCreationInputDTO();
		ValidationDataDTO validationInfo = new ValidationDataDTO();
		validationInfo.setCdaValidated(false);
		validationInfo.setWorkflowInstanceId(MISSING_WORKFLOW_PLACEHOLDER);
		
		validation.setValidationData(validationInfo);

		String transformId = ""; 

		try {
			final JWTTokenDTO jwtToken;
			if (Boolean.TRUE.equals(msCfg.getFromGovway())) {
				jwtToken = extractAndValidateJWT(request.getHeader(Headers.JWT_GOVWAY_HEADER), msCfg.getFromGovway());
			} else {
				jwtToken = extractAndValidateJWT(request.getHeader(Headers.JWT_HEADER), msCfg.getFromGovway());
			}
			validation.setJwtToken(jwtToken);

			PublicationCreationReqDTO jsonObj = getAndValidatePublicationReq(request.getParameter("requestBody"), isReplace);
			validation.setJsonObj(jsonObj);

			final byte[] bytePDF = getAndValidateFile(file);
			validation.setFile(bytePDF);
			
			final String cda = extractCDA(bytePDF, jsonObj.getMode());
			validation.setCda(cda);
			
			validateJWT(validation.getJwtToken(), cda);
			
			final org.jsoup.nodes.Document docT = Jsoup.parse(cda);
			final String key = extractFieldCda(docT);
			validation.setDocument(docT);
			validation.setKafkaKey(key);
	
			validationInfo = getValidationInfo(cda, jsonObj.getWorkflowInstanceId());
			validation.setValidationData(validationInfo); // Updating validation info

			ValidationDataDTO validatedDocument = cdaSRV.getByWorkflowInstanceId(validationInfo.getWorkflowInstanceId()); 

			if(validationInfo.isAccreditamento()) {
				String wiiAcc = CdaUtility.getWorkflowInstanceId(docT);
				validationInfo.setWorkflowInstanceId(wiiAcc);
				validatedDocument.setAccreditamento(true);
				validatedDocument.setCdaValidated(true);
				validatedDocument.setWorkflowInstanceId(wiiAcc);
				validatedDocument.setTransformID(docT.select("templateid").get(0).attr("root"));
			}
			
			transformId = validatedDocument.getTransformID(); 
			cdaSRV.consumeHash(validationInfo.getHash()); 

			if(!validationInfo.isAccreditamento() 
					&& DateUtility.getDifferenceDays(validatedDocument.getInsertionDate(), new Date()) > validationCFG.getDaysAllowToPublishAfterValidation()) {
				final ErrorResponseDTO error = ErrorResponseDTO.builder()
						.type(OLDER_DAY.getType())
						.title(OLDER_DAY.getTitle())
						.instance(ErrorInstanceEnum.OLDER_DAY.getInstance())
						.detail("Error: cannot publish documents older than " + validationCFG.getDaysAllowToPublishAfterValidation() + " days").build();
				throw new ValidationException(error); 
			}
			
			final String documentSha256 = encodeSHA256(bytePDF);
			validation.setDocumentSha(documentSha256);
	
			validateDocumentHash(documentSha256, validation.getJwtToken());
	
			final ResourceDTO fhirResourcesDTO = documentReferenceSRV.createFhirResources(cda, jsonObj, bytePDF.length, documentSha256,
				validation.getJwtToken().getPayload().getPerson_id(), transformId,validationInfo.isAccreditamento());
	
			validation.setFhirResource(fhirResourcesDTO);
			
			if(!isNullOrEmpty(fhirResourcesDTO.getErrorMessage())) {
				final ErrorResponseDTO error = ErrorResponseDTO.builder()
					.type(FHIR_MAPPING_ERROR.getType())
					.title(FHIR_MAPPING_ERROR.getTitle())
					.instance(ErrorInstanceEnum.FHIR_RESOURCE_ERROR.getInstance())
					.detail(fhirResourcesDTO.getErrorMessage()).build();
	
				throw new ValidationException(error);
			}
			
		} catch (final ValidationException ve) {
			cdaSRV.consumeHash(validationInfo.getHash());
			validation.setValidationError(ve);
		}

		return validation;
	}
	
	@Override
	public ResponseWifDTO delete(String idDoc, HttpServletRequest request) {
		final Date startOperation = new Date();
		// Create request tracking
		LogTraceInfoDTO log = getLogTraceInfo();
		String workflowInstanceId = createWorkflowInstanceId(idDoc);

		JWTTokenDTO token = null;
		String role = Constants.App.JWT_MISSING_SUBJECT_ROLE;
		String subjectFiscalCode = Constants.App.JWT_MISSING_SUBJECT;
		String locality = Constants.App.JWT_MISSING_LOCALITY;
		String warning = null;

		String subjApplicationId = null;
		String subjApplicationVendor = null;
		String subjApplicationVersion = null;
		
		try {
			// Extract token
			token = extractFromReqJWT(request);
			// Extract subject role
			role = token.getPayload().getSubject_role();
			locality = token.getPayload().getLocality();
			
			subjApplicationId = token.getPayload().getSubject_application_id(); 
			subjApplicationVendor = token.getPayload().getSubject_application_vendor();
			subjApplicationVersion = token.getPayload().getSubject_application_version();
			
			
			subjectFiscalCode = CfUtility.extractFiscalCodeFromJwtSub(token.getPayload().getSub());

			// ==============================
			// [1] Retrieve reference from INI
			// ==============================
			IniReferenceResponseDTO iniReference = iniClient.reference(new IniReferenceRequestDTO(idDoc, token.getPayload()));
			// Exit if necessary
			if(!isNullOrEmpty(iniReference.getErrorMessage())) {
				kafkaSRV.sendDeleteStatus(log.getTraceID(), workflowInstanceId, idDoc, iniReference.getErrorMessage(), BLOCKING_ERROR, token.getPayload(), RIFERIMENTI_INI);
				throw new IniException(iniReference.getErrorMessage());	
			} else {
				kafkaSRV.sendDeleteStatus(log.getTraceID(), workflowInstanceId, idDoc, "Riferimenti trovati: " +iniReference.getUuid(), SUCCESS, token.getPayload(), RIFERIMENTI_INI);
			}

			// ==============================
			// [2] Send delete request to EDS
			// ==============================
			EdsResponseDTO edsResponse = edsClient.delete(idDoc);
			// Exit if necessary
			Objects.requireNonNull(edsResponse, "PublicationCTL returned an error - edsResponse is null!");

			if (!edsResponse.isEsito()) {
				// Update transaction status
				kafkaSRV.sendDeleteStatus(log.getTraceID(), workflowInstanceId, idDoc, edsResponse.getMessageError(), BLOCKING_ERROR, token.getPayload(), EDS_DELETE);
				throw new EdsException("Error encountered while sending delete information to EDS client");
			} else {
				// Update transaction status
				kafkaSRV.sendDeleteStatus(log.getTraceID(), workflowInstanceId, idDoc, "Delete effettuata su eds", SUCCESS, token.getPayload(), EDS_DELETE);
			}


			// ==============================
			// [3] Send delete request to INI
			// ==============================
			DeleteRequestDTO deleteRequestDTO = buildRequestForIni(idDoc, iniReference.getUuid(), token,iniReference.getDocumentType(),
					subjApplicationId, subjApplicationVendor, subjApplicationVersion);
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
				kafkaSRV.sendDeleteStatus(log.getTraceID(), workflowInstanceId, idDoc, "Transazione presa in carico", EventStatusEnum.ASYNC_RETRY, token.getPayload(),
						INI_DELETE);
				warning = Misc.WARN_ASYNC_TRANSACTION;
			} else {
				// Update transaction status
				kafkaSRV.sendDeleteStatus(log.getTraceID(), workflowInstanceId, idDoc, "Delete effettuata su ini", SUCCESS, token.getPayload(), INI_DELETE);
			}

			logger.info(String.format("Deletion of CDA completed for document with identifier %s", idDoc), OperationLogEnum.DELETE_CDA2, ResultLogEnum.OK, startOperation, token.getPayload().getIss(), MISSING_DOC_TYPE_PLACEHOLDER, role, subjectFiscalCode, locality,
					subjApplicationId, subjApplicationVendor,subjApplicationVersion);
		} catch(MockEnabledException me) {
			throw me;
		} catch(IniException inEx) {
			final String issuer = token != null ? token.getPayload().getIss() : JWT_MISSING_ISSUER_PLACEHOLDER;

			logger.error(String.format("Error while delete record from ini %s", idDoc), OperationLogEnum.DELETE_CDA2, ResultLogEnum.KO, startOperation, INI_EXCEPTION.getErrorCategory(), issuer, MISSING_DOC_TYPE_PLACEHOLDER, role, subjectFiscalCode, locality,
					subjApplicationId, subjApplicationVendor,subjApplicationVersion);
			throw inEx;
			
		} catch (Exception e) {
			final String issuer = token != null ? token.getPayload().getIss() : JWT_MISSING_ISSUER_PLACEHOLDER;
			RestExecutionResultEnum errorInstance = RestExecutionResultEnum.GENERIC_ERROR;
			if (e instanceof ValidationException) {
				errorInstance = get(((ValidationException) e).getError().getType());
			}

			logger.error(String.format("Error while deleting CDA of document with identifier %s", idDoc), OperationLogEnum.DELETE_CDA2, ResultLogEnum.KO, startOperation, errorInstance.getErrorCategory(), issuer, MISSING_DOC_TYPE_PLACEHOLDER, role, subjectFiscalCode, locality,
					subjApplicationId, subjApplicationVendor,subjApplicationVersion);
			throw e;
		}
		
		return new ResponseWifDTO(workflowInstanceId, log, warning);
	}
	
	private DeleteRequestDTO buildRequestForIni(final String identificativoDocumento, final String uuid, final JWTTokenDTO jwtTokenDTO,
			final String documentType, String applicationId, String applicationVendor, String applicationVersion) {
		DeleteRequestDTO out = null;
		try {
			JWTPayloadDTO jwtPayloadDTO = jwtTokenDTO.getPayload();
			out = DeleteRequestDTO.builder().
					action_id(jwtPayloadDTO.getAction_id()).
					idDoc(identificativoDocumento).
					uuid(uuid).
					iss(jwtPayloadDTO.getIss()).
					locality(jwtPayloadDTO.getLocality()).
					patient_consent(jwtPayloadDTO.getPatient_consent()).
					person_id(jwtPayloadDTO.getPerson_id()).
					purpose_of_use(jwtPayloadDTO.getPurpose_of_use()).
					resource_hl7_type(jwtPayloadDTO.getResource_hl7_type()).
					sub(jwtPayloadDTO.getSub()).
					subject_organization_id(jwtPayloadDTO.getSubject_organization_id()).
					subject_organization(jwtPayloadDTO.getSubject_organization()).
					subject_role(jwtPayloadDTO.getSubject_role()).
					documentType(documentType).
					subject_application_id(applicationId).
					subject_application_vendor(applicationVendor).
					subject_application_version(applicationVersion).
					build();
		} catch(Exception ex) {
			log.error("Error while build request delete for ini : " , ex);
			throw new BusinessException("Error while build request delete for ini : " , ex);
		}
		return out;
	}

}
