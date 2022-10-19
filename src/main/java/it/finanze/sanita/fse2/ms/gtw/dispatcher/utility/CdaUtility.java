/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.utility;

import org.jsoup.nodes.Document;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class CdaUtility {
	
	private static String templateId = null;
	

	private static String templateIdExtension = null;
	
	public static String getTemplateIdExtension() {
		return templateIdExtension;
	}
	
	public static String getTemplateId() {
		return templateId;
	}

	/**
	 * Private constructor to avoid instantiation.
	 */
	private CdaUtility() {
		// Constructor intentionally empty.
	}

	
	public static String getWorkflowInstanceId(final org.jsoup.nodes.Document docT) {

		try {
			String cxi = extractInfo(docT);	
			return cxi + "." + StringUtility.generateTransactionUID(null) + "^^^^urn:ihe:iti:xdw:2013:workflowInstanceId";
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
			out = id + "." + StringUtility.encodeSHA256Hex(extension);
		} catch(Exception ex) {
			log.error("Error while extracting info from cda", ex);
			throw new BusinessException("Error while extracting info from cda", ex);
		}
		return out;
	}

	public static String getDocumentType(final Document cdaDocument) {
		String docType = Constants.App.MISSING_DOC_TYPE_PLACEHOLDER;
		if (cdaDocument != null) {
			final String extractedDocType = extractDocumentType(cdaDocument);
			if (!StringUtility.isNullOrEmpty(extractedDocType)) {
				docType = extractedDocType;
			}
		}
		return docType;
	}

	private static String extractDocumentType(final Document docT) {
		String out = "";
		try {
			out = docT.select("code").get(0).attr("displayName");
		} catch(Exception ex) {
			log.error("Error while extracting document type from CDA2", ex);
			throw new BusinessException("Error while extracting document type from CDA2", ex);
		}
		return out;
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
