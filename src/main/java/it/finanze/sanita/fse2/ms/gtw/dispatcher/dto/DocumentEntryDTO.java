/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DocumentEntryDTO {

	private String mimeType;
	private String creationTime;
	private String hash;
	private Integer size;
	private String administrativeRequest;
	private String author;
	private String authorRole;
	private String authorInstitution;
	private String languageCode;
	private String repositoryUniqueId;
	private String patientId;
	private String conservazioneANorma;
	private String firma;
	private List<String> description;
	private String serviceStartTime;
	private String serviceStopTime;
	private String classCode;
	private String classCodeName;
	private String confidentialityCode;
	private String confidentialityCodeDisplayName;
	private String formatCode;
	private String formatCodeName;
	private List<String> eventCodeList;
	private String healthcareFacilityTypeCode;
	private String healthcareFacilityTypeCodeName;
	private String practiceSettingCode;
	private String practiceSettingCodeName;
	private String typeCode;
	private String typeCodeName;
	private String uniqueId;
	 
}
