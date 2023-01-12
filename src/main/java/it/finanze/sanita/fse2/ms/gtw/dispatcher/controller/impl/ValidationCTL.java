/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.IValidationCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ValidationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.logging.LoggerHelper;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IErrorHandlerSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CfUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;

/**
 * Validation controller.
 */
@RestController
public class ValidationCTL extends AbstractCTL implements IValidationCTL {

	@Autowired
	private IKafkaSRV kafkaSRV;

	@Autowired
	private LoggerHelper logger;

	@Autowired
	private IErrorHandlerSRV errorHandlerSRV;

	@Override
	public ResponseEntity<ValidationResDTO> validate(final ValidationCDAReqDTO requestBody, final MultipartFile file, final HttpServletRequest request) {
		final Date startDateOperation = new Date();
		LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();

		String workflowInstanceId = Constants.App.MISSING_WORKFLOW_PLACEHOLDER;
		JWTTokenDTO jwtToken = null;
		ValidationCDAReqDTO jsonObj = null;
		String warning = null;
		Document docT = null;

		String role = Constants.App.JWT_MISSING_SUBJECT_ROLE;
		String subjectFiscalCode = Constants.App.JWT_MISSING_SUBJECT;

		String subjApplicationId = null;
		String subjApplicationVendor = null;
		String subjApplicationVersion = null;
		
		try {
			if (Boolean.TRUE.equals(msCfg.getFromGovway())) {
				jwtToken = extractAndValidateJWT(request.getHeader(Constants.Headers.JWT_GOVWAY_HEADER),
						msCfg.getFromGovway(), EventTypeEnum.VALIDATION);
			} else {
				jwtToken = extractAndValidateJWT(request.getHeader(Constants.Headers.JWT_HEADER),
						msCfg.getFromGovway(), EventTypeEnum.VALIDATION);
			}

			role = jwtToken.getPayload().getSubject_role();
			subjectFiscalCode = CfUtility.extractFiscalCodeFromJwtSub(jwtToken.getPayload().getSub());
			jsonObj = getAndValidateValidationReq(request.getParameter("requestBody"));
			final byte[] bytes = getAndValidateFile(file);
			final String cda = extractCDA(bytes, jsonObj.getMode());
			docT = Jsoup.parse(cda);
			workflowInstanceId = CdaUtility.getWorkflowInstanceId(docT);

			validateJWT(jwtToken, cda);
			
			subjApplicationId = jwtToken.getPayload().getSubject_application_id(); 
			subjApplicationVendor = jwtToken.getPayload().getSubject_application_vendor();
			subjApplicationVersion = jwtToken.getPayload().getSubject_application_version();
			
			warning = validate(cda, jsonObj.getActivity(), workflowInstanceId);
			String message = null;
			if (jsonObj.getActivity().equals(ActivityEnum.VERIFICA)) {
				message = "Attenzione - è stato chiamato l'endpoint di validazione con VERIFICA";
			}

			kafkaSRV.sendValidationStatus(traceInfoDTO.getTraceID(), workflowInstanceId, EventStatusEnum.SUCCESS,message, jwtToken != null ? jwtToken.getPayload() : null);

			String issuer = (jwtToken != null && jwtToken.getPayload() != null
					&& !StringUtility.isNullOrEmpty(jwtToken.getPayload().getIss())) ? jwtToken.getPayload().getIss()
							: Constants.App.JWT_MISSING_ISSUER_PLACEHOLDER;
			String locality = (jwtToken != null && jwtToken.getPayload() != null
					&& !StringUtility.isNullOrEmpty(jwtToken.getPayload().getLocality())) ? jwtToken.getPayload().getLocality()
							: Constants.App.JWT_MISSING_LOCALITY;

			logger.info("Validation CDA completed for workflow instance Id " + workflowInstanceId, OperationLogEnum.VAL_CDA2, ResultLogEnum.OK, startDateOperation, issuer, CdaUtility.getDocumentType(docT), role, subjectFiscalCode, locality,
					subjApplicationId,subjApplicationVendor,subjApplicationVersion);
			request.setAttribute("JWT_ISSUER", issuer);
		} catch (final ValidationException e) {
			errorHandlerSRV.validationExceptionHandler(startDateOperation, traceInfoDTO, workflowInstanceId, jwtToken,
					e, CdaUtility.getDocumentType(docT));
		}

		if (jsonObj != null && jsonObj.getMode() == null) {
			String schematronWarn = StringUtility.isNullOrEmpty(warning) ? "" : warning;
			warning = "[" + schematronWarn + "[WARNING_EXTRACT]" + Constants.Misc.WARN_EXTRACTION_SELECTION + "]";
		}

		warning = StringUtility.isNullOrEmpty(warning) ? null : warning;
		if (jsonObj != null && ActivityEnum.VALIDATION.equals(jsonObj.getActivity())) {
			return new ResponseEntity<>(new ValidationResDTO(traceInfoDTO, workflowInstanceId, warning),
					HttpStatus.CREATED);
		}

		return new ResponseEntity<>(new ValidationResDTO(traceInfoDTO, workflowInstanceId, warning), HttpStatus.OK);
	}
}
