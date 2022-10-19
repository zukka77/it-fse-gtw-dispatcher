/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Model to save ini and eds invocation info.
 */
@Document(collection = "#{@iniEdsInvocationBean}")
@Data
@NoArgsConstructor
public class IniEdsInvocationETY {

	@Id
	private String id;
	
	@Field(name = "workflow_instance_id")
	private String workflowInstanceId;

	@Field(name = "identificativo_doc")
	private String idDoc;
	
	@Field(name = "data")
	private org.bson.Document data;
	
	@Field(name = "metadata")
	private List<org.bson.Document> metadata;
	
	 
}