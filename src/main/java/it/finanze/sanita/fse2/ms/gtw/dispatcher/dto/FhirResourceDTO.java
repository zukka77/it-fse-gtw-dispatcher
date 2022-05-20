package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FhirResourceDTO {

	private String errorMessage;
	
	private String documentReferenceJson;
	
	private String submissionSetEntryJson;
	
	private String documentEntryJson;
}
