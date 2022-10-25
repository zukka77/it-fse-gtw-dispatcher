package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) 
public class WorkflowIdException extends RuntimeException{

	public WorkflowIdException(String message) {
		super(message);
	}
	
}
