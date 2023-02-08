package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model to save ini and eds invocation info.
 */
@Document(collection = "#{@auditBean}")
@Data
@NoArgsConstructor
public class AuditETY {

	private String servizio;
	
	private Date start_time;
	
	private Date end_time;
	
	private Object request;
	
	private Object response;
	
	private String jwt_issuer;
	
	private String httpMethod;
}
