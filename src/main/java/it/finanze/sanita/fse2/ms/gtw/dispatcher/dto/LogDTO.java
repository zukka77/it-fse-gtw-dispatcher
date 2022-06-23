package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import lombok.Builder;

@Builder
public class LogDTO {

	final String log_type = "gateway-structured-log";
	
	String message;
	
	String operation;
	
	String op_result;
	
	String op_timestamp_start;
	
	String op_timestamp_end;
	
	String op_error;
	
	String op_error_description;
	
}
