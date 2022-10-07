package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import java.util.Date;

import javax.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Guido Rocco
 *
 *	DTO used for validated documents. 
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ValidatedDocumentsDTO extends AbstractDTO {
	
	/**
	 * Serial Version UID 
	 */
	private static final long serialVersionUID = 1646851675311902079L; 	
	
	
	@Size(min = 0, max = 64)
	private String hashCda; 
	
	@Size(min = 0, max = 1000)
	private String workflowInstanceId; 
	
	@Size(min = 0, max = 1000)
	private String primaryKeyTransf; 
	
	private Date insertionDate; 

}
