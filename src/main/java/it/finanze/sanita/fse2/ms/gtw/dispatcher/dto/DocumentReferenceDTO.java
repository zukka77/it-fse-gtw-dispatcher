/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentReferenceDTO {
	
	private Integer size;
	
	private String hash;

	private String facilityTypeCode;
	
	private List<String> eventCode;
	
	private String practiceSettingCode;
	
	private String patientID;
	
	private String tipoDocumentoLivAlto;
	
	private String repositoryUniqueID;
	
	private String serviceStartTime;
	
	private String serviceStopTime;

	private String encodedCDA;

	private String identificativoDoc;
	
}
