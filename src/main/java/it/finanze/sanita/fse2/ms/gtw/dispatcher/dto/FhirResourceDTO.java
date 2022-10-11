package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import lombok.Data;

@Data
public class FhirResourceDTO {

	private DocumentReferenceDTO documentReferenceDTO;
	
	private String cda;
	
	private String objectId;  
	
}
