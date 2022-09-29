package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import java.util.Date;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.TSPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;

public interface IErrorHandlerSRV {

    /**
     * Handle validation exception for validation ep
     * @param startDateOperation
     * @param traceInfoDTO
     * @param workflowInstanceId
     * @param jwtToken
     * @param e
     */
    void validationExceptionHandler(Date startDateOperation, LogTraceInfoDTO traceInfoDTO, String workflowInstanceId, JWTTokenDTO jwtToken, ValidationException e, String documentType);

    /**
     * Handle connection refused exception for publication ep
     * @param requestBody
     * @param startDateOperation
     * @param validationInfo
     * @param jwtToken
     * @param jsonObj
     * @param traceInfoDTO
     * @param ex
     * @param isPublication
     */
    void connectionRefusedExceptionHandler(Date startDateOperation, ValidationDataDTO validationInfo, JWTTokenDTO jwtToken, 
            PublicationCreationReqDTO jsonObj, LogTraceInfoDTO traceInfoDTO, ConnectionRefusedException ex,
            boolean isPublication, String documentType);
    /**
     * Handle validation exception for publication ep.
     * 
     * @param startDateOperation
     * @param validationInfo
     * @param jwtToken
     * @param jsonObj
     * @param traceInfoDTO
     * @param e
     * @param isPublication
     */
    void publicationValidationExceptionHandler(Date startDateOperation, ValidationDataDTO validationInfo, JWTTokenDTO jwtToken, 
            PublicationCreationReqDTO jsonObj, LogTraceInfoDTO traceInfoDTO, ValidationException e, boolean isPublication, String documentType);
    /**
     * Handle connection refused exception for ts feeding ep
     * @param startDateOperation
     * @param workflowInstanceId
     * @param jwtToken
     * @param jsonObj
     * @param traceInfoDTO
     * @param e
     */
    void tsFeedingValidationExceptionHandler(Date startDateOperation, String workflowInstanceId, JWTTokenDTO jwtToken, TSPublicationCreationReqDTO jsonObj, LogTraceInfoDTO traceInfoDTO, ValidationException e, String documentType);

    /**
     * Handle validation exception for ts feeding ep
     * @param startDateOperation
     * @param workflowInstanceId
     * @param jwtToken
     * @param jsonObj
     * @param traceInfoDTO
     * @param ex
     */
    void tsFeedingConnectionRefusedExceptionHandler(Date startDateOperation, String workflowInstanceId, JWTTokenDTO jwtToken, TSPublicationCreationReqDTO jsonObj, LogTraceInfoDTO traceInfoDTO, ConnectionRefusedException ex, String documentType);

}
