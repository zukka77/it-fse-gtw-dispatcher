package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model to save validated documents info.
 */
@Document(collection = "#{@validatedDocumentsBean}")
@Data
@NoArgsConstructor
public class ValidatedDocumentsETY {

	@Id
	private String id; 
	
	@Field(name = "hash_cda")
	private String hashCda; 
	
	@Field(name = "w_id")
	private String workflowInstanceId; 
	
	@Field(name = "pkey_transf")
	private String primaryKeyTransf; 
	
	@Field(name = "pkey_schematron")
	private String primaryKeySchematron; 
	
	@Field(name = "pkey_schema")
	private String primaryKeySchema; 
	
	@Field(name = "insertion_date")
	private Date insertionDate;

	public static ValidatedDocumentsETY setContent(String hash, String wii) {
        ValidatedDocumentsETY entity = new ValidatedDocumentsETY();
        Date now = new Date();
        entity.setHashCda(hash);
		entity.setWorkflowInstanceId(wii);
        entity.setInsertionDate(now);
        return entity;
    }
}


