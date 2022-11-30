package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatusCheckDTO {

	private String eventType;

	private Date eventDate;  
	
	private String eventStatus;
	
	private String message;

	private String identificativoDocumento;

	private String subject;

	private String subjectRole;
	
	private String tipoAttivita;
	
	private String organizzazione;
	
	private String workflowInstanceId;
	
	private String traceId;
	
	private String issuer;
	
	private Date expiringDate;
	
}
