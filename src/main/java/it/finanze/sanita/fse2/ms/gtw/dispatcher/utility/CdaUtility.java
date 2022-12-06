/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.utility;

import org.jsoup.nodes.Document;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.DocumentTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import lombok.extern.slf4j.Slf4j;

import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility.*;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility.encodeSHA256Hex;

@Slf4j
public final class CdaUtility {
	
	private static final String WIF_SUFFIX = "^^^^urn:ihe:iti:xdw:2013:workflowInstanceId";
	private static final String WIF_SEPARATOR = ".";
	/**
	 * Private constructor to avoid instantiation.
	 */
	private CdaUtility() {
		// Constructor intentionally empty.
	}

	public static String createWorkflowInstanceId(final String idDoc) {
		return encodeSHA256Hex(idDoc) + WIF_SEPARATOR + generateTransactionUID() + WIF_SUFFIX;
	}

	public static String getWorkflowInstanceId(final org.jsoup.nodes.Document docT) {

		try {
			String cxi = extractInfo(docT);	
			return cxi + WIF_SEPARATOR + generateTransactionUID() + WIF_SUFFIX;
		} catch (Exception e) {
			final ErrorResponseDTO error = ErrorResponseDTO.builder()
				.title(RestExecutionResultEnum.WORKFLOW_ID_ERROR.getTitle())
				.type(RestExecutionResultEnum.WORKFLOW_ID_ERROR.getType())
				.instance(RestExecutionResultEnum.WORKFLOW_ID_ERROR.getType())
				.detail("Errore durante l'estrazione del workflow instance id ").build();
			
			throw new ValidationException(error);
		}
	}
	
	private static String extractInfo(final Document docT) {
		String out = "";
		try {
			String id = docT.select("id").get(0).attr("root");
			String extension = docT.select("id").get(0).attr("extension");
			out = id + "." + encodeSHA256Hex(extension);
		} catch(Exception ex) {
			log.error("Error while extracting info from cda", ex);
			throw new BusinessException("Error while extracting info from cda", ex);
		}
		return out;
	}

	public static String getDocumentType(final Document cdaDocument) {
		String docType = Constants.App.MISSING_DOC_TYPE_PLACEHOLDER;
		if (cdaDocument != null) {
			final String code = cdaDocument.select("code").get(0).attr("code");
			DocumentTypeEnum extractedDocType = DocumentTypeEnum.getByCode(code);
			if (extractedDocType != null) {
				docType = extractedDocType.getDocumentType();
			}
		}
		
		return isNullOrEmpty(docType) ? Constants.App.MISSING_DOC_TYPE_PLACEHOLDER : docType;
	}

	public static String extractFieldCda(final org.jsoup.nodes.Document docT) {
		String out = "";
		try {
			//root
			String templateId = docT.select("templateid").get(0).attr("root");
			//extension
			String templateIdExtension = docT.select("templateid").get(0).attr("extension");
			out = templateId + "_" + templateIdExtension;
		} catch(Exception ex) {
			log.error("Error while extracting info for schematron ", ex);
			throw new BusinessException("Error while extracting info for schematron ", ex);
		}

		return out;
	}
}
