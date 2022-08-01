package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.TSPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ErrorInstanceEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationErrorException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationPublicationErrorException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.logging.ElasticLoggerHelper;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.ICdaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IErrorHandlerSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.ErrorUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;

@Service
public class ErrorHandlerSRV implements IErrorHandlerSRV {

    @Autowired
    private ErrorUtility errorUtility;

    @Autowired
    private IKafkaSRV kafkaSRV;

    @Autowired
    private ICdaSRV cdaSRV;

    @Autowired
    private ElasticLoggerHelper elasticLogger;

    @Override
    public void connectionRefusedExceptionHandler(Date startDateOperation, ValidationDataDTO validationInfo, JWTTokenDTO jwtToken, 
        PublicationCreationReqDTO jsonObj, LogTraceInfoDTO traceInfoDTO, ConnectionRefusedException ex) {
        if (jsonObj == null || !Boolean.TRUE.equals(jsonObj.isForcePublish())) {
            cdaSRV.consumeHash(validationInfo.getHash());
        }

        String errorMessage = ex.getMessage();
        String capturedErrorType = RestExecutionResultEnum.FHIR_MAPPING_TIMEOUT.getType();
        String errorCategory = RestExecutionResultEnum.FHIR_MAPPING_TIMEOUT.getErrorCategory().getCode();
        String errorInstance = ErrorInstanceEnum.NO_INFO.getInstance();

        EventStatusEnum errorEventStatus = errorUtility.computeErrorStatus(errorCategory);

        kafkaSRV.sendPublicationStatus(
                traceInfoDTO.getTraceID(), validationInfo.getWorkflowInstanceId(), errorEventStatus,
                errorMessage, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);

        final RestExecutionResultEnum errorType = RestExecutionResultEnum.get(capturedErrorType);

        String issuer = (jwtToken != null && jwtToken.getPayload() != null && !StringUtility.isNullOrEmpty(jwtToken.getPayload().getIss())) ? jwtToken.getPayload().getIss() : "ISSUER_UNDEFINED";
        elasticLogger.error(errorMessage + " " + validationInfo.getWorkflowInstanceId(), OperationLogEnum.PUB_CDA2, ResultLogEnum.KO, startDateOperation, errorType.getErrorCategory(), issuer);
        throw new ValidationPublicationErrorException(errorType, StringUtility.sanitizeMessage(errorType.getTitle()), errorInstance);
    }

    @Override
    public void publicationValidationExceptionHandler(Date startDateOperation, ValidationDataDTO validationInfo, JWTTokenDTO jwtToken, 
        PublicationCreationReqDTO jsonObj, LogTraceInfoDTO traceInfoDTO, ValidationException e) {
        if (jsonObj == null || !Boolean.TRUE.equals(jsonObj.isForcePublish())) {
            cdaSRV.consumeHash(validationInfo.getHash());
        }

        String errorMessage = e.getMessage();
        String capturedErrorType = RestExecutionResultEnum.GENERIC_ERROR.getType();
        String errorCategory = RestExecutionResultEnum.FHIR_MAPPING_TIMEOUT.getErrorCategory().getCode();
        String errorInstance = ErrorInstanceEnum.NO_INFO.getInstance();

        if (e.getError() != null) {
            errorMessage = e.getError().getDetail();
            capturedErrorType = e.getError().getType();
            errorInstance = e.getError().getInstance();
        }

        EventStatusEnum errorEventStatus = errorUtility.computeErrorStatus(errorCategory);

        kafkaSRV.sendPublicationStatus(
                traceInfoDTO.getTraceID(), validationInfo.getWorkflowInstanceId(), errorEventStatus,
                errorMessage, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
        

        final RestExecutionResultEnum errorType = RestExecutionResultEnum.get(capturedErrorType);

        String issuer = (jwtToken != null && jwtToken.getPayload() != null && !StringUtility.isNullOrEmpty(jwtToken.getPayload().getIss())) ? jwtToken.getPayload().getIss() : "ISSUER_UNDEFINED";
        elasticLogger.error(errorMessage + " " + validationInfo.getWorkflowInstanceId(), OperationLogEnum.PUB_CDA2, ResultLogEnum.KO, startDateOperation, errorType.getErrorCategory(), issuer);
        throw new ValidationPublicationErrorException(errorType,StringUtility.sanitizeMessage(errorMessage), errorInstance);
    }

    @Override
    public void tsFeedingValidationExceptionHandler(Date startDateOperation, String workflowInstanceId, JWTTokenDTO jwtToken, TSPublicationCreationReqDTO jsonObj, LogTraceInfoDTO traceInfoDTO, ValidationException e) {
        String errorMessage = e.getMessage();
        String capturedErrorType = RestExecutionResultEnum.GENERIC_ERROR.getType();
        String errorCategory = RestExecutionResultEnum.FHIR_MAPPING_TIMEOUT.getErrorCategory().getCode();
        String errorInstance = ErrorInstanceEnum.NO_INFO.getInstance();
        if (e.getError() != null) {
            errorMessage = e.getError().getDetail();
            capturedErrorType = e.getError().getType();
            errorInstance = e.getError().getInstance();
        }

        final RestExecutionResultEnum result = RestExecutionResultEnum.get(capturedErrorType);
        EventStatusEnum errorEventStatus = errorUtility.computeErrorStatus(errorCategory);

        kafkaSRV.sendFeedingStatus(traceInfoDTO.getTraceID(), workflowInstanceId, errorEventStatus, errorMessage, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);

        String issuer = (jwtToken != null && jwtToken.getPayload()!= null && !StringUtility.isNullOrEmpty(jwtToken.getPayload().getIss())) ? jwtToken.getPayload().getIss() : Constants.App.JWT_MISSING_ISSUER_PLACEHOLDER;
        elasticLogger.error(errorMessage + " " + workflowInstanceId, OperationLogEnum.PUB_CDA2, ResultLogEnum.KO, startDateOperation, result.getErrorCategory(),issuer);
        throw new ValidationPublicationErrorException(result, StringUtility.sanitizeMessage(e.getError().getDetail()), errorInstance);
    }

    @Override
    public void tsFeedingConnectionRefusedExceptionHandler(Date startDateOperation, String workflowInstanceId, JWTTokenDTO jwtToken, TSPublicationCreationReqDTO jsonObj, LogTraceInfoDTO traceInfoDTO, ConnectionRefusedException ex) {
        String errorMessage = ex.getMessage();
        String capturedErrorType = RestExecutionResultEnum.FHIR_MAPPING_TIMEOUT.getType();
        String errorCategory = RestExecutionResultEnum.FHIR_MAPPING_TIMEOUT.getErrorCategory().getCode();
        String errorInstance = ErrorInstanceEnum.NO_INFO.getInstance();

        final RestExecutionResultEnum result = RestExecutionResultEnum.get(capturedErrorType);
        EventStatusEnum errorEventStatus = errorUtility.computeErrorStatus(errorCategory);

        String issuer = (jwtToken != null && jwtToken.getPayload() != null && !StringUtility.isNullOrEmpty(jwtToken.getPayload().getIss())) ? jwtToken.getPayload().getIss() : Constants.App.JWT_MISSING_ISSUER_PLACEHOLDER;

        kafkaSRV.sendFeedingStatus(traceInfoDTO.getTraceID(), workflowInstanceId, errorEventStatus, errorMessage, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
        elasticLogger.error(errorMessage + " " + workflowInstanceId, OperationLogEnum.PUB_CDA2, ResultLogEnum.KO, startDateOperation, result.getErrorCategory(),issuer);
        throw new ValidationPublicationErrorException(result, StringUtility.sanitizeMessage(ex.getMessage()), errorInstance);
    }

    @Override
    public void validationExceptionHandler(Date startDateOperation, LogTraceInfoDTO traceInfoDTO, String workflowInstanceId, JWTTokenDTO jwtToken, ValidationException e) {
        String errorMessage = e.getMessage();
        String capturedErrorType = RestExecutionResultEnum.GENERIC_ERROR.getType();
        String errorCategory = RestExecutionResultEnum.FHIR_MAPPING_TIMEOUT.getErrorCategory().getCode();
        String errorInstance = ErrorInstanceEnum.NO_INFO.getInstance();

        if (e.getError() != null) {
            errorMessage = e.getError().getDetail();
            capturedErrorType = e.getError().getType();
            errorInstance = e.getError().getInstance();
        }

        final RestExecutionResultEnum validationResult = RestExecutionResultEnum.get(capturedErrorType);
        EventStatusEnum errorEventStatus = errorUtility.computeErrorStatus(errorCategory);
        kafkaSRV.sendValidationStatus(traceInfoDTO.getTraceID(), workflowInstanceId, errorEventStatus, errorMessage, jwtToken != null ? jwtToken.getPayload() : null);

        String issuer = (jwtToken !=null && jwtToken.getPayload()!=null && !StringUtility.isNullOrEmpty(jwtToken.getPayload().getIss())) ? jwtToken.getPayload().getIss() : Constants.App.JWT_MISSING_ISSUER_PLACEHOLDER;
        elasticLogger.error(e.getError().getDetail() + " " + workflowInstanceId, OperationLogEnum.VAL_CDA2, ResultLogEnum.KO, startDateOperation, validationResult.getErrorCategory(), issuer);
        throw new ValidationErrorException(validationResult, StringUtility.sanitizeMessage(e.getError().getDetail()), workflowInstanceId, errorInstance);
    }

}
