/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
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
import it.finanze.sanita.fse2.ms.gtw.dispatcher.logging.LoggerHelper;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.ICdaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IErrorHandlerSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CfUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;

@Service
public class ErrorHandlerSRV implements IErrorHandlerSRV {

    @Autowired
    private IKafkaSRV kafkaSRV;

    @Autowired
    private ICdaSRV cdaSRV;

    @Autowired
    private LoggerHelper logger;

    @Override
    public void connectionRefusedExceptionHandler(Date startDateOperation, ValidationDataDTO validationInfo, JWTPayloadDTO jwtPayloadToken, 
        PublicationCreationReqDTO jsonObj, LogTraceInfoDTO traceInfoDTO, ConnectionRefusedException ex,
        boolean isPublication, final String documentType) {
        if (jsonObj == null) {
            cdaSRV.consumeHash(validationInfo.getHash());
        }

        String errorMessage = ex.getMessage();
        String capturedErrorType = RestExecutionResultEnum.GENERIC_TIMEOUT.getType();
        String errorInstance = ErrorInstanceEnum.NO_INFO.getInstance();
        
        EventStatusEnum errorEventStatus = RestExecutionResultEnum.GENERIC_TIMEOUT.getEventStatusEnum();

        kafkaSRV.sendPublicationStatus(
                traceInfoDTO.getTraceID(), validationInfo.getWorkflowInstanceId(), errorEventStatus,
                errorMessage, jsonObj, jwtPayloadToken);

        final RestExecutionResultEnum errorType = RestExecutionResultEnum.get(capturedErrorType);

        String issuer = (jwtPayloadToken != null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getIss())) ? jwtPayloadToken.getIss() : "ISSUER_UNDEFINED";
        String role = (jwtPayloadToken.getSubject_role()!=null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getSubject_role())) ? jwtPayloadToken.getSubject_role() : Constants.App.JWT_MISSING_SUBJECT_ROLE;
        String subjectFiscalCode = CfUtility.extractFiscalCodeFromJwtSub(jwtPayloadToken.getSub());
        String locality = (jwtPayloadToken.getLocality()!=null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getLocality())) ? jwtPayloadToken.getLocality() : Constants.App.JWT_MISSING_LOCALITY;
        
        String subjectApplicationId = (jwtPayloadToken != null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getSubject_application_id())) ? jwtPayloadToken.getSubject_application_id() : "SUBJECT APP ID UNDEFINED";
        String subjectApplicationVendor = (jwtPayloadToken != null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getSubject_application_vendor())) ? jwtPayloadToken.getSubject_application_vendor() : "SUBJECT APP VENDOR UNDEFINED";
        String subjectApplicationVersion = (jwtPayloadToken != null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getSubject_application_version())) ? jwtPayloadToken.getSubject_application_version() : "SUBJECT APP VERSION UNDEFINED";
        
        if(isPublication) {
        	logger.error(Constants.App.LOG_TYPE_CONTROL,validationInfo.getWorkflowInstanceId(),errorMessage + " " + validationInfo.getWorkflowInstanceId(), OperationLogEnum.PUB_CDA2, ResultLogEnum.KO, startDateOperation, errorType.getErrorCategory(), issuer, documentType, role, subjectFiscalCode, locality,
        			subjectApplicationId, subjectApplicationVendor,subjectApplicationVersion);
        } else {
        	logger.error(Constants.App.LOG_TYPE_CONTROL,validationInfo.getWorkflowInstanceId(),errorMessage + " " + validationInfo.getWorkflowInstanceId(), OperationLogEnum.REPLACE_CDA2, ResultLogEnum.KO, startDateOperation, errorType.getErrorCategory(), issuer, documentType, role, subjectFiscalCode, locality,
        			subjectApplicationId, subjectApplicationVendor,subjectApplicationVersion);
        }
        throw new ValidationPublicationErrorException(errorType, StringUtility.sanitizeMessage(errorType.getTitle()), errorInstance);
    }

    @Override
    public void publicationValidationExceptionHandler(Date startDateOperation, ValidationDataDTO validationInfo, JWTPayloadDTO jwtPayloadToken, 
        PublicationCreationReqDTO jsonObj, LogTraceInfoDTO traceInfoDTO, ValidationException e, boolean isPublication, final String documentType) {
        if (jsonObj == null) {
            cdaSRV.consumeHash(validationInfo.getHash());
        }

        String errorMessage = e.getMessage();
        String capturedErrorType = RestExecutionResultEnum.GENERIC_ERROR.getType();
        String errorInstance = ErrorInstanceEnum.NO_INFO.getInstance();
        EventStatusEnum errorEventStatus =  RestExecutionResultEnum.GENERIC_ERROR.getEventStatusEnum();
        
        if (e.getError() != null) {
            errorMessage = e.getError().getDetail();
            capturedErrorType = e.getError().getType();
            errorInstance = e.getError().getInstance();
            errorEventStatus = RestExecutionResultEnum.get(capturedErrorType).getEventStatusEnum();
        }

        if(isPublication) {
        	kafkaSRV.sendPublicationStatus(traceInfoDTO.getTraceID(), validationInfo.getWorkflowInstanceId(), errorEventStatus,
        			errorMessage, jsonObj, jwtPayloadToken);
        } else {
        	kafkaSRV.sendReplaceStatus(traceInfoDTO.getTraceID(), validationInfo.getWorkflowInstanceId(), errorEventStatus,
        			errorMessage, jsonObj, jwtPayloadToken);
        }
        

        final RestExecutionResultEnum errorType = RestExecutionResultEnum.get(capturedErrorType);

        String issuer = (jwtPayloadToken != null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getIss())) ? jwtPayloadToken.getIss() : "ISSUER_UNDEFINED";
        String role = (jwtPayloadToken.getSubject_role()!=null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getSubject_role())) ? jwtPayloadToken.getSubject_role() : Constants.App.JWT_MISSING_SUBJECT_ROLE;
        String subjectFiscalCode = CfUtility.extractFiscalCodeFromJwtSub(jwtPayloadToken.getSub());
        String locality = (jwtPayloadToken.getLocality()!=null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getLocality())) ? jwtPayloadToken.getLocality() : Constants.App.JWT_MISSING_LOCALITY;
        
        String subjectApplicationId = (jwtPayloadToken != null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getSubject_application_id())) ? jwtPayloadToken.getSubject_application_id() : "SUBJECT APP ID UNDEFINED";
        String subjectApplicationVendor = (jwtPayloadToken != null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getSubject_application_vendor())) ? jwtPayloadToken.getSubject_application_vendor() : "SUBJECT APP VENDOR UNDEFINED";
        String subjectApplicationVersion = (jwtPayloadToken != null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getSubject_application_version())) ? jwtPayloadToken.getSubject_application_version() : "SUBJECT APP VERSION UNDEFINED";
       
        if(isPublication) {
        	logger.error(Constants.App.LOG_TYPE_CONTROL,validationInfo.getWorkflowInstanceId(),errorMessage + " " + validationInfo.getWorkflowInstanceId(), OperationLogEnum.PUB_CDA2, ResultLogEnum.KO, startDateOperation, errorType.getErrorCategory(), issuer, documentType, role, subjectFiscalCode, locality,
        			subjectApplicationId,subjectApplicationVendor,subjectApplicationVersion);
        } else {
        	logger.error(Constants.App.LOG_TYPE_CONTROL,validationInfo.getWorkflowInstanceId(),errorMessage + " " + validationInfo.getWorkflowInstanceId(), OperationLogEnum.REPLACE_CDA2, ResultLogEnum.KO, startDateOperation, errorType.getErrorCategory(), issuer, documentType, role, subjectFiscalCode, locality,
        			subjectApplicationId,subjectApplicationVendor,subjectApplicationVersion);
        }
        throw new ValidationPublicationErrorException(errorType,StringUtility.sanitizeMessage(errorMessage), errorInstance);
    }

    @Override
    public void validationExceptionHandler(Date startDateOperation, LogTraceInfoDTO traceInfoDTO, String workflowInstanceId, JWTPayloadDTO jwtPayloadToken, 
        ValidationException e, final String documentType) {
        
        String errorMessage = e.getMessage();
        String capturedErrorType = RestExecutionResultEnum.GENERIC_ERROR.getType();
        String errorInstance = ErrorInstanceEnum.NO_INFO.getInstance();
        EventStatusEnum errorEventStatus =  RestExecutionResultEnum.GENERIC_ERROR.getEventStatusEnum();
        if (e.getError() != null) {
            errorMessage = e.getError().getDetail();
            capturedErrorType = e.getError().getType();
            errorInstance = e.getError().getInstance();
            errorEventStatus = RestExecutionResultEnum.get(capturedErrorType).getEventStatusEnum();
        }

        final RestExecutionResultEnum validationResult = RestExecutionResultEnum.get(capturedErrorType);
        kafkaSRV.sendValidationStatus(traceInfoDTO.getTraceID(), workflowInstanceId, errorEventStatus, errorMessage, jwtPayloadToken);

        String issuer = (jwtPayloadToken!=null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getIss())) ? jwtPayloadToken.getIss() : Constants.App.JWT_MISSING_ISSUER_PLACEHOLDER;
        String role = (jwtPayloadToken.getSubject_role()!=null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getSubject_role())) ? jwtPayloadToken.getSubject_role() : Constants.App.JWT_MISSING_SUBJECT_ROLE;
        String subjectFiscalCode = CfUtility.extractFiscalCodeFromJwtSub(jwtPayloadToken.getSub());
        String locality = (jwtPayloadToken.getLocality()!=null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getLocality())) ? jwtPayloadToken.getLocality() : Constants.App.JWT_MISSING_LOCALITY;
        
        String subjectApplicationId = (jwtPayloadToken != null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getSubject_application_id())) ? jwtPayloadToken.getSubject_application_id() : "SUBJECT APP ID UNDEFINED";
        String subjectApplicationVendor = (jwtPayloadToken != null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getSubject_application_vendor())) ? jwtPayloadToken.getSubject_application_vendor() : "SUBJECT APP VENDOR UNDEFINED";
        String subjectApplicationVersion = (jwtPayloadToken != null && !StringUtility.isNullOrEmpty(jwtPayloadToken.getSubject_application_version())) ? jwtPayloadToken.getSubject_application_version() : "SUBJECT APP VERSION UNDEFINED";
       
        if (RestExecutionResultEnum.VOCABULARY_ERROR != RestExecutionResultEnum.get(capturedErrorType)) {
        	logger.error(Constants.App.LOG_TYPE_CONTROL,workflowInstanceId,e.getError().getDetail() + " " + workflowInstanceId, OperationLogEnum.VAL_CDA2, ResultLogEnum.KO, startDateOperation, validationResult.getErrorCategory(), issuer, documentType, role, subjectFiscalCode, locality,
        			subjectApplicationId,subjectApplicationVendor,subjectApplicationVersion);
        }
        throw new ValidationErrorException(validationResult, StringUtility.sanitizeMessage(e.getError().getDetail()), workflowInstanceId, errorInstance);
    }

}
