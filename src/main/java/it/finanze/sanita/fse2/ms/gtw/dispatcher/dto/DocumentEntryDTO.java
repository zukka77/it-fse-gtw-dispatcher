package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DocumentEntryDTO {

	private String mimeType;
	private String entryUUID;
	private Date creationTime;
	private String hash;
	private Integer size;
	private String status;
	private String languageCode;

	private String patientId;
	private String confidentialityCode;
	private String confidentialityCodeDisplayName;
	private String typeCode;
	private String typeCodeName;
	private String formatCode;
	private String formatCodeName;
	private String legalAuthenticator;
	private String sourcePatientInfo;
	private String author;
	private String representedOrganizationName;
	private String representedOrganizationCode;
	private String uniqueId;
	private List<String> referenceIdList;

	private String healthcareFacilityTypeCode;
	private String healthcareFacilityTypeCodeName;
	private List<String> eventCodeList;
	private String repositoryUniqueId;
	private String classCode;
	private String classCodeName;
	private String practiceSettingCode;
	private String practiceSettingCodeName;
	private String sourcePatientId;
	private Date serviceStartTime;
	private Date serviceStopTime;
}
