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
	
	@Field(name = "pkey_struct")
	private String primaryKeyStructure; 
	
	@Field(name = "insertion_date")
	private Date insertionDate;

	public static ValidatedDocumentsETY setContent(String hash, String wii, String objectID, String transformID) {
        ValidatedDocumentsETY entity = new ValidatedDocumentsETY();
        entity.setHashCda(hash);
		entity.setWorkflowInstanceId(wii);
		entity.setPrimaryKeyTransf(objectID);
		entity.setPrimaryKeyStructure(transformID); 
        return entity;
    }
}


