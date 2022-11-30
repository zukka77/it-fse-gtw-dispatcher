package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatusCheckDTO {

	@Size(min = 0, max = 100)
	private String eventType;

	@Size(min = 0, max = 100)
	private String eventDate;  
	
	@Size(min = 0, max = 100)
	private String eventStatus;
	
	@Size(min = 0, max = 10000)
	private String message;

	@Size(min = 0, max = 100)
	private String identificativoDocumento;

	@Size(min = 0, max = 100)
	private String subject;

	@Size(min = 0, max = 100)
	private String subjectRole;
	
	@Size(min = 0, max = 100)
	private String tipoAttivita;
	
	@Size(min = 0, max = 100)
	private String organizzazione;
	
	@Size(min = 0, max = 256)
	private String workflowInstanceId;
	
	@Size(min = 0, max = 100)
	private String traceId;
	
	@Size(min = 0, max = 100)
	private String issuer;
	
	@Size(min = 0, max = 100)
	private String expiringDate;
	
}
