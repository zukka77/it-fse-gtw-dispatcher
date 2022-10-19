/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmissionSetEntryDTO {

	private String submissionTime;
	private String sourceId;
	private String contentTypeCode;
	private String contentTypeCodeName;
	private String uniqueID;
	
}
