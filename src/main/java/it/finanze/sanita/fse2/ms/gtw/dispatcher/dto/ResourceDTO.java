package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResourceDTO {

	private String errorMessage;
	
	private String bundleJson;
	
	private String submissionSetEntryJson;
	
	private String documentEntryJson;
}
