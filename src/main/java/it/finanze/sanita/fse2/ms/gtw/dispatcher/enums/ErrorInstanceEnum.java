/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorInstanceEnum {

	NO_INFO("", "No specific information for this error, refeer to type for any info"),
	CDA_EXTRACTION("/cda-extraction", "Error while extracting CDA from PDF document"),
	CDA_NOT_VALIDATED("/cda-validation", "Error while retrieving information about CDA validation"),
	DIFFERENT_HASH("/jwt-hash-match", "Hash of document different from hash in JWT"),
	MISSING_MANDATORY_ELEMENT("/request-missing-field", "Missing required field in request body"),
	INVALID_DATE_FORMAT("/request-invalid-date-format", "Field of type date not correctly inputed"),
	SEMANTIC_WARNING("/schematron-malformed/warning", "Schematron malformed with non-blocking problem"),
	SEMANTIC_ERROR("/schematron-malformed/error", "Schematron malformed with blocking error"),
	DOCUMENT_TYPE_MISMATCH("/jwt-document-type", "Mismatch on document type from JWT to CDA"),
	PERSON_ID_MISMATCH("/jwt-person-id", "Mismatch on person-id from JWT to CDA"),
	MISSING_JWT("/missing-jwt", "JWT token completely missing"),
	MISSING_JWT_FIELD("/jwt-mandatory-field-missing", "Mandatory field in JWT is missing"),
	JWT_MALFORMED_FIELD("/jwt-mandatory-field-malformed", "Malformed JWT field"),
	FHIR_RESOURCE_ERROR("/fhir-resource", "Error creating fhir resource"),
	NON_PDF_FILE("/multipart-file", "File type must be a PDF document"),
	EMPTY_FILE("/empty-multipart-file", "File type must not be empty"),
	OLDER_DAY("/msg/max-day-limit-exceed", "Cannot publish documents older"),
	EDS_DOCUMENT_MISSING("/msg/eds-document-missing", "Document cannot be found on the Server FHIR"),
	SIMULATION_EXCEPTION("/msg/simulation-error", "Simulation error");
	
	private String instance;
	private String description;

	public static ErrorInstanceEnum get(String inInstance) {
		ErrorInstanceEnum out = null;
		for (ErrorInstanceEnum v: ErrorInstanceEnum.values()) {
			if (v.getInstance().equalsIgnoreCase(inInstance)) {
				out = v;
				break;
			}
		}
		return out;
	}


}