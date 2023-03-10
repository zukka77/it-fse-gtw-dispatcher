/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.utility;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.DocumentTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;

import java.util.regex.Pattern;

import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ErrorInstanceEnum.INVALID_ID_ERROR;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum.INVALID_ID_DOC;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility.*;
import static org.apache.commons.lang3.StringUtils.isWhitespace;

@Slf4j
public final class CdaUtility {

	private static final String MASTER_ID_SEPARATOR = "^";
	private static final Pattern MASTER_ID_PTT = Pattern.compile("^[\\w.]+\\^[\\w.]+$");
	
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

	/**
	 * Evaluate an identifier and validate it
	 *
	 * @param id The master identifier
	 * @return {@code true} if the identifier is well-formed
	 */
	public static boolean isValidMasterId(String id) {
		boolean valid = false;
		// Check argument consistency
		if (id != null && !id.isEmpty() && !isWhitespace(id)) {
			// If it contains separator, it must match expected shape
			if (id.contains(MASTER_ID_SEPARATOR)) {
				// Check for <text><separator><text> (e.g abc^dfg)
				if(MASTER_ID_PTT.matcher(id).matches()) {
					// It's required at least another word after separator
					// No need to fear IndexOutOfBoundsException
					String param = id.substring(id.indexOf(MASTER_ID_SEPARATOR) + 1);
					// Check for emptiness
					valid = !param.isEmpty() && !isWhitespace(param);
				}
			} else {
				valid = true;
			}
		}
		// Return value
		return valid;
	}

	public static ErrorResponseDTO createMasterIdError() {
		return ErrorResponseDTO.builder()
			.title(INVALID_ID_DOC.getTitle())
			.type(INVALID_ID_DOC.getType())
			.instance(INVALID_ID_ERROR.getInstance())
			.detail(INVALID_ID_ERROR.getDescription())
			.build();
	}

}
