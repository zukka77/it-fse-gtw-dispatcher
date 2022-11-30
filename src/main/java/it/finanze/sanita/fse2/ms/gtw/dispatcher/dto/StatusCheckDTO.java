package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatusCheckDTO {

	String eventType;

	Date eventDate;  
	
	String eventStatus;
	
	String message;

	String identificativoDocumento;

	String subject;

	String subjectRole;
	
	String tipoAttivita;
	
	String organizzazione;
	
	String workflowInstanceId;
	
	String traceId;
	
	String issuer;
	
	Date expiringDate;
	
}
