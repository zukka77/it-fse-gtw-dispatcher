package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmissionSetEntryDTO {

	private Date submissionTime;
	private String sourceId;
	private String contentTypeCode;
	private String contentTypeCodeName;
	private String uniqueID;
	
}
