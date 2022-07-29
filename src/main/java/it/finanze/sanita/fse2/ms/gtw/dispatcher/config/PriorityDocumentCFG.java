package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class PriorityDocumentCFG {

	/**
	 * List of low priority documents.
	 */
	@Value("${document-type.priority.low}")
	private String lowPriorityDocuments;
	
	/**
	 * List of medium priority documents.
	 */
	@Value("${document-type.priority.medium}")
	private String mediumPriorityDocuments;

	/**
	 * List of high priority documents.
	 */
	@Value("${document-type.priority.high}")
	private String highPriorityDocuments;
}
