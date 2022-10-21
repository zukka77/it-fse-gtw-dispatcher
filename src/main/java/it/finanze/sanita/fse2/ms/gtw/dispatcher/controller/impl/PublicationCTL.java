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

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IIniClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
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
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationMetadataReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniTraceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.PublicationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ResponseDTO;
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
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.ProfileUtility;
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
	private transient LoggerHelper logger;

	@Autowired
	private ICdaFacadeSRV cdaSRV;

	@Autowired
	private transient IErrorHandlerSRV errorHandlerSRV;
	
	@Autowired
	private IIniClient iniClient;
	
	@Autowired
	private IEdsClient edsClient;

	@Autowired
	private ProfileUtility profileUtils;

	@Override
	public ResponseEntity<PublicationResDTO> create(final PublicationCreationReqDTO requestBody, final MultipartFile file, final HttpServletRequest request) {
		final Date startDateOperation = new Date();
		final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();

		ValidationCreationInputDTO validationInfo = new ValidationCreationInputDTO();
		validationInfo.setValidationData(new ValidationDataDTO(null, false, Constants.App.MISSING_WORKFLOW_PLACEHOLDER));

		String role = null;
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

			final IndexerValueDTO kafkaValue = new IndexerValueDTO();
			kafkaValue.setWorkflowInstanceId(validationInfo.getValidationData().getWorkflowInstanceId());
			kafkaValue.setIdDoc(validationInfo.getJsonObj().getIdentificativoDoc());
			kafkaValue.setEdsDPOperation(ProcessorOperationEnum.PUBLISH);


			kafkaSRV.notifyChannel(validationInfo.getKafkaKey(), new Gson().toJson(kafkaValue), priorityType, validationInfo.getJsonObj().getTipoDocumentoLivAlto(), DestinationTypeEnum.INDEXER);
			kafkaSRV.sendPublicationStatus(traceInfoDTO.getTraceID(), validationInfo.getValidationData().getWorkflowInstanceId(), EventStatusEnum.SUCCESS, null, validationInfo.getJsonObj(), validationInfo.getJwtToken() != null ? validationInfo.getJwtToken().getPayload() : null);
			
			role = validationInfo.getJwtToken().getPayload().getSubject_role();
			logger.info(String.format("Publication CDA completed for workflow instance id %s", validationInfo.getValidationData().getWorkflowInstanceId()), OperationLogEnum.PUB_CDA2, ResultLogEnum.OK, startDateOperation, validationInfo.getJwtToken().getPayload().getIss(), CdaUtility.getDocumentType(validationInfo.getDocument()), role);
		} catch (ConnectionRefusedException ce) {
			errorHandlerSRV.connectionRefusedExceptionHandler(startDateOperation, validationInfo.getValidationData(), validationInfo.getJwtToken(), validationInfo.getJsonObj(), traceInfoDTO, ce,
					true, CdaUtility.getDocumentType(validationInfo.getDocument()));
		} catch (final ValidationException e) {
			errorHandlerSRV.publicationValidationExceptionHandler(startDateOperation, validationInfo.getValidationData(), validationInfo.getJwtToken(), validationInfo.getJsonObj(), traceInfoDTO, e,
					true, CdaUtility.getDocumentType(validationInfo.getDocument()));
		}

		String warning = null;
		
		if (validationInfo.getJsonObj().getMode() == null) {
			warning = Constants.Misc.WARN_EXTRACTION_SELECTION;
		}
		
		return new ResponseEntity<>(new PublicationResDTO(traceInfoDTO, warning, validationInfo.getValidationData().getWorkflowInstanceId()), HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<PublicationResDTO> replace(final String idDoc, final PublicationUpdateReqDTO requestBody,
													 final MultipartFile file, final HttpServletRequest request) {
		
			final Date startDateOperation = new Date();
			final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();

			ValidationCreationInputDTO validationInfo = new ValidationCreationInputDTO();
			validationInfo.setValidationData(new ValidationDataDTO(null, false, Constants.App.MISSING_WORKFLOW_PLACEHOLDER));

			String role = null;
			try {
				validationInfo = validateInput(file, request, true);

				if (validationInfo.getValidationError() != null) {
					throw validationInfo.getValidationError();
				}

				log.debug("Executing replace of document: {}", idDoc);
				iniInvocationSRV.replace(validationInfo.getValidationData().getWorkflowInstanceId(), validationInfo.getFhirResource(), validationInfo.getJwtToken(), idDoc);
				
				final IndexerValueDTO kafkaValue = new IndexerValueDTO();
				kafkaValue.setWorkflowInstanceId(validationInfo.getValidationData().getWorkflowInstanceId());
				kafkaValue.setIdDoc(idDoc);
				kafkaValue.setEdsDPOperation(ProcessorOperationEnum.REPLACE);
				
				kafkaSRV.notifyChannel(validationInfo.getKafkaKey(), new Gson().toJson(kafkaValue), PriorityTypeEnum.LOW, validationInfo.getJsonObj().getTipoDocumentoLivAlto(), DestinationTypeEnum.INDEXER);
				kafkaSRV.sendReplaceStatus(traceInfoDTO.getTraceID(), validationInfo.getValidationData().getWorkflowInstanceId(), EventStatusEnum.SUCCESS, null, validationInfo.getJsonObj(), validationInfo.getJwtToken() != null ? validationInfo.getJwtToken().getPayload() : null);

				role = validationInfo.getJwtToken().getPayload().getSubject_role();
				logger.info(String.format("Replace CDA completed for workflow instance id %s", validationInfo.getValidationData().getWorkflowInstanceId()), OperationLogEnum.REPLACE_CDA2, ResultLogEnum.OK, startDateOperation, validationInfo.getJwtToken().getPayload().getIss(), CdaUtility.getDocumentType(validationInfo.getDocument()),
						role);
			} catch (ConnectionRefusedException ce) {
				errorHandlerSRV.connectionRefusedExceptionHandler(startDateOperation, validationInfo.getValidationData(), validationInfo.getJwtToken(), validationInfo.getJsonObj(), traceInfoDTO, ce,
						false, CdaUtility.getDocumentType(validationInfo.getDocument()));
			} catch (final ValidationException e) {
				errorHandlerSRV.publicationValidationExceptionHandler(startDateOperation, validationInfo.getValidationData(), validationInfo.getJwtToken(), validationInfo.getJsonObj(), traceInfoDTO, e,
						false, CdaUtility.getDocumentType(validationInfo.getDocument()));
			}
	
			String warning = null;
			
			if (validationInfo.getJsonObj().getMode() == null) {
				warning = Constants.Misc.WARN_EXTRACTION_SELECTION;
			}
			
			return new ResponseEntity<>(new PublicationResDTO(traceInfoDTO, warning, validationInfo.getValidationData().getWorkflowInstanceId()), HttpStatus.OK);
	}


	@Override
	public ResponseEntity<ResponseDTO> updateMetadata(final String idDoc, final PublicationMetadataReqDTO requestBody, final HttpServletRequest request) {
		
		final boolean isTestEnvironment = profileUtils.isDevOrDockerProfile();
		
		// Estrazione token
		JWTTokenDTO jwtToken = null;
		final Date startDateOperation = new Date();
		String role = null;
		try {
			if (Boolean.TRUE.equals(msCfg.getFromGovway())) {
				jwtToken = extractAndValidateJWT(request.getHeader(Constants.Headers.JWT_GOVWAY_HEADER), msCfg.getFromGovway());
			} else {
				jwtToken = extractAndValidateJWT(request.getHeader(Constants.Headers.JWT_HEADER), msCfg.getFromGovway());
			}

			role = jwtToken.getPayload().getSubject_role();
			PublicationMetadataReqDTO jsonObj = StringUtility.fromJSONJackson(request.getParameter("requestBody"), PublicationMetadataReqDTO.class);

			final IniTraceResponseDTO iniResponse = iniClient.updateMetadati(new IniMetadataUpdateReqDTO(idDoc, jwtToken.getPayload(), jsonObj));
			
			EdsResponseDTO edsResponse = null;
			if (iniResponse.getEsito() || isTestEnvironment) {
				log.debug("Ini response is OK, proceeding with EDS");
			    edsResponse = edsClient.update(new EdsMetadataUpdateReqDTO(idDoc, null, jsonObj));
			} else {
				throw new IniException("Error encountered while sending update information to INI client");
			}

			if (!isTestEnvironment && (edsResponse == null || !edsResponse.getEsito())) {
				throw new EdsException("Error encountered while sending update information to EDS client");
			}

			boolean iniMockMessage = !StringUtility.isNullOrEmpty(iniResponse.getErrorMessage()) && iniResponse.getErrorMessage().contains("Invalid region ip");
			
			if (isTestEnvironment && iniMockMessage) {
				throw new MockEnabledException(iniResponse.getErrorMessage(), edsResponse != null ? edsResponse.getErrorMessage() : null);
			}

			 
			logger.info(String.format("Update of CDA metadata completed for document with identifier %s", idDoc), OperationLogEnum.UPDATE_METADATA_CDA2, ResultLogEnum.OK, startDateOperation, jwtToken.getPayload().getIss(), Constants.App.MISSING_DOC_TYPE_PLACEHOLDER,
					role);
		} catch (MockEnabledException me) {
			throw me;
		} catch (Exception e) {
			final String issuer = jwtToken != null ? jwtToken.getPayload().getIss() : Constants.App.JWT_MISSING_ISSUER_PLACEHOLDER;
			RestExecutionResultEnum errorInstance = RestExecutionResultEnum.GENERIC_ERROR;
			if (e instanceof ValidationException) {
				errorInstance = RestExecutionResultEnum.get(((ValidationException) e).getError().getType());
			}
			
			log.error(String.format("Error encountered while updating CDA metadata with identifier %s", idDoc), e);
			logger.error(String.format("Error while updating CDA metadata of document with identifier %s", idDoc), OperationLogEnum.UPDATE_METADATA_CDA2, ResultLogEnum.KO, startDateOperation, errorInstance.getErrorCategory(), issuer, Constants.App.MISSING_DOC_TYPE_PLACEHOLDER, role);
			throw e;
		}
		
		return new ResponseEntity<>(new ResponseDTO(getLogTraceInfo()), HttpStatus.OK);
	}

	private ValidationCreationInputDTO validateInput(final MultipartFile file, final HttpServletRequest request, final boolean isReplace) {

		final ValidationCreationInputDTO validation = new ValidationCreationInputDTO();
		ValidationDataDTO validationInfo = new ValidationDataDTO();
		validationInfo.setCdaValidated(false);
		validationInfo.setWorkflowInstanceId(Constants.App.MISSING_WORKFLOW_PLACEHOLDER);
		
		validation.setValidationData(validationInfo);

		try {
			final JWTTokenDTO jwtToken;
			if (Boolean.TRUE.equals(msCfg.getFromGovway())) {
				jwtToken = extractAndValidateJWT(request.getHeader(Constants.Headers.JWT_GOVWAY_HEADER), msCfg.getFromGovway());
			} else {
				jwtToken = extractAndValidateJWT(request.getHeader(Constants.Headers.JWT_HEADER), msCfg.getFromGovway());
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
			final String key = CdaUtility.extractFieldCda(docT);
			validation.setDocument(docT);
			validation.setKafkaKey(key);
	
			if (!Boolean.TRUE.equals(jsonObj.isForcePublish()) || isReplace) {
				validationInfo = getValidationInfo(cda, jsonObj.getWorkflowInstanceId());
			} else {
				validationInfo.setWorkflowInstanceId(CdaUtility.getWorkflowInstanceId(docT));
			}
	
			validation.setValidationData(validationInfo); // Updating validation info

			if (!Boolean.TRUE.equals(jsonObj.isForcePublish()) || isReplace) {
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
	
	@Override
	public ResponseDTO delete(String idDoc, HttpServletRequest request) {
		
		final Date startDateOperation = new Date();
		final boolean isTestEnvironment = profileUtils.isDevOrDockerProfile();
		JWTTokenDTO jwtToken = null;
		String role = null;
		try {
			if (Boolean.TRUE.equals(msCfg.getFromGovway())) {
				jwtToken = extractAndValidateJWT(request.getHeader(Constants.Headers.JWT_GOVWAY_HEADER), msCfg.getFromGovway());
			} else {
				jwtToken = extractAndValidateJWT(request.getHeader(Constants.Headers.JWT_HEADER), msCfg.getFromGovway());
			}
			
			role = jwtToken.getPayload().getSubject_role();
			
			final DeleteRequestDTO iniReq = buildRequestForIni(idDoc, jwtToken);
			final IniTraceResponseDTO iniResponse = iniClient.delete(iniReq);
			
			EdsResponseDTO edsResponse = null;
			if (iniResponse.getEsito() || isTestEnvironment) {
				log.debug("Ini response is OK, proceeding with EDS");
				edsResponse = edsClient.delete(idDoc);
			} else {
				throw new IniException("Error encountered while sending delete information to INI client");
			}

			if (!isTestEnvironment && (edsResponse == null || !edsResponse.getEsito())) {
				throw new EdsException("Error encountered while sending delete information to EDS client");
			}

			boolean iniMockMessage = !StringUtility.isNullOrEmpty(iniResponse.getErrorMessage()) && iniResponse.getErrorMessage().contains("Invalid region ip");
			
			if (isTestEnvironment && iniMockMessage) {
				throw new MockEnabledException(iniResponse.getErrorMessage(), edsResponse != null ? edsResponse.getErrorMessage() : null);
			}

			logger.info(String.format("Deletion of CDA completed for document with identifier %s", idDoc), OperationLogEnum.DELETE_CDA2, ResultLogEnum.OK, startDateOperation, jwtToken.getPayload().getIss(), Constants.App.MISSING_DOC_TYPE_PLACEHOLDER,
					role);
		} catch(MockEnabledException me) {
			throw me;
		} catch (Exception e) {
			final String issuer = jwtToken != null ? jwtToken.getPayload().getIss() : Constants.App.JWT_MISSING_ISSUER_PLACEHOLDER;
			RestExecutionResultEnum errorInstance = RestExecutionResultEnum.GENERIC_ERROR;
			if (e instanceof ValidationException) {
				errorInstance = RestExecutionResultEnum.get(((ValidationException) e).getError().getType());
			}

			log.error(String.format("Error encountered while deleting CDA with identifier %s", idDoc), e);
			logger.error(String.format("Error while deleting CDA of document with identifier %s", idDoc), OperationLogEnum.DELETE_CDA2, ResultLogEnum.KO, startDateOperation, errorInstance.getErrorCategory(), issuer, Constants.App.MISSING_DOC_TYPE_PLACEHOLDER,
					role);
			throw e;
		}
		
		return new ResponseDTO(getLogTraceInfo());
	}
	
	private DeleteRequestDTO buildRequestForIni(final String identificativoDocumento, final JWTTokenDTO jwtTokenDTO) {
		DeleteRequestDTO out = null;
		try {
			JWTPayloadDTO jwtPayloadDTO = jwtTokenDTO.getPayload();
			out = DeleteRequestDTO.builder().
					action_id(jwtPayloadDTO.getAction_id()).
					idDoc(identificativoDocumento).
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
					build();
		} catch(Exception ex) {
			log.error("Error while build request delete for ini : " , ex);
			throw new BusinessException("Error while build request delete for ini : " , ex);
		}
		return out;
	}

}
