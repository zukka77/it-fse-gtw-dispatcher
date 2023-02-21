/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import java.util.Date;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
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
    void validationExceptionHandler(Date startDateOperation, LogTraceInfoDTO traceInfoDTO, String workflowInstanceId, JWTPayloadDTO jwtPayloadToken,  ValidationException e, String documentType);
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
    void connectionRefusedExceptionHandler(Date startDateOperation, ValidationDataDTO validationInfo, JWTPayloadDTO jwtPayloadToken, 
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
    void publicationValidationExceptionHandler(Date startDateOperation, ValidationDataDTO validationInfo, JWTPayloadDTO jwtPayloadToken, 
            PublicationCreationReqDTO jsonObj, LogTraceInfoDTO traceInfoDTO, ValidationException e, boolean isPublication, final String documentType);
    
}
