package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.handler;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import brave.Tracer;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.exceptions.RecordNotFoundException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.exceptions.ServerResponseException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ValidationErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ErrorInstanceEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationErrorException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationPublicationErrorException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 *	Exceptions Handler.
 */
@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

	
	/**
	 * Tracker log.
	 */
	@Autowired
	private Tracer tracer;
   

	/**
	 * Management validation exception.
	 * 
	 * @param ex		exception
	 * @param request	request
	 * @return			
	 */
    @ExceptionHandler(value = {ValidationErrorException.class})
    protected ResponseEntity<ValidationErrorResponseDTO> handleValidationException(final ValidationErrorException ex, final WebRequest request) {
    	log.error("" , ex);  
    	Integer status = 400;
    	if (RestExecutionResultEnum.SEMANTIC_ERROR.equals(ex.getResult())) {
        	status = 422;
    	}
    	
    	if (RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.equals(ex.getResult()) || 
    			RestExecutionResultEnum.INVALID_TOKEN_FIELD.equals(ex.getResult())) {
        	status = 403;
    	}
    	
     	
    	if(RestExecutionResultEnum.DOCUMENT_TYPE_ERROR.equals(ex.getResult())) {
    		status = 415;
    	}
    	
      	String workflowInstanceId = StringUtility.isNullOrEmpty(ex.getWorkflowInstanceId()) ? null : ex.getWorkflowInstanceId(); 
    	
    	ValidationErrorResponseDTO out = new ValidationErrorResponseDTO(getLogTraceInfo(), ex.getResult().getType(), ex.getResult().getTitle(), ex.getMessage(), status, ex.getErrorInstance(), workflowInstanceId);
    	
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        
    	return new ResponseEntity<>(out, headers, status);
    }

	@ExceptionHandler(value = {ValidationException.class})
    protected ResponseEntity<ErrorResponseDTO> handleGenericValidationException(final ValidationException ex, final WebRequest request) {
		log.error("" , ex);  
    	Integer status = 400;
    	if (RestExecutionResultEnum.SEMANTIC_ERROR.equals(RestExecutionResultEnum.get(ex.getError().getType()))) {
        	status = 422;
    	}
    	
    	if (RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.equals(RestExecutionResultEnum.get(ex.getError().getType()))) {
        	status = 401;
    	}
    	
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        
    	return new ResponseEntity<>(ex.getError(), headers, status);
    }

    /**
	 * Management connection refused exception.
	 * 
	 * @param ex		exception
	 * @param request	request
	 * @return			
	 */
    @ExceptionHandler(value = {ConnectionRefusedException.class})
    protected ResponseEntity<ErrorResponseDTO> handleConnectionRefusedException(final ConnectionRefusedException ex, final WebRequest request) {
    	log.error("" , ex);  
    	Integer status = 500;
		
    	String detailedMessage = ex.getMessage(); 
    	ErrorResponseDTO out = new ErrorResponseDTO(getLogTraceInfo(), "/msg/connection-refused", ex.getMessage(), detailedMessage, status, "/msg/connection-refused");

    	HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        
    	return new ResponseEntity<>(out, headers, status);
    }
    
    /**
  	 * Management generic server response exception.
  	 * 
  	 * @param ex		exception
  	 * @param request	request
  	 * @return			
  	 */
      @ExceptionHandler(value = {ServerResponseException.class})
      protected ResponseEntity<ErrorResponseDTO> handleServerException(final ServerResponseException ex, final WebRequest request) {
      	log.error("" , ex);  
      	Integer status = 500;
      	
      	ErrorResponseDTO out = new ErrorResponseDTO(getLogTraceInfo(), RestExecutionResultEnum.SERVER_ERROR.getType(), RestExecutionResultEnum.SERVER_ERROR.getTitle(), ExceptionUtils.getMessage(ex), status, ErrorInstanceEnum.NO_INFO.getInstance());

      	HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
          
      	return new ResponseEntity<>(out, headers, status);
      }


	/**
  	 * Management record not found exception received by clients.
  	 * 
  	 * @param ex		exception
  	 * @param request	request
  	 * @return			
  	 */
      @ExceptionHandler(value = {RecordNotFoundException.class})
      protected ResponseEntity<ErrorResponseDTO> handleRecordNotFoundException(final RecordNotFoundException ex, final WebRequest request) {
      	log.error("" , ex);  
      	Integer status = 404;
      	
      	ErrorResponseDTO out = new ErrorResponseDTO(getLogTraceInfo(), RestExecutionResultEnum.RECORD_NOT_FOUND.getType(), RestExecutionResultEnum.RECORD_NOT_FOUND.getTitle(), ExceptionUtils.getMessage(ex), status, ErrorInstanceEnum.NO_INFO.getInstance());

      	HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
          
      	return new ResponseEntity<>(out, headers, status);
      }
    
    
    
	/**
	 * Management generic exception.
	 * 
	 * @param ex		exception
	 * @param request	request
	 * @return			
	 */
    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<ErrorResponseDTO> handleGenericException(final Exception ex, final WebRequest request) {
    	Integer status = 500;

		ErrorResponseDTO out = new ErrorResponseDTO(getLogTraceInfo(), RestExecutionResultEnum.GENERIC_ERROR.getType(), RestExecutionResultEnum.GENERIC_ERROR.getTitle(), "Errore generico", status, ErrorInstanceEnum.NO_INFO.getInstance());

    	HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        
    	return new ResponseEntity<>(out, headers, status);
    }

	@ExceptionHandler(value = {BusinessException.class})
    protected ResponseEntity<ErrorResponseDTO> handleBusinessException(final BusinessException ex, final WebRequest request) {
    	Integer status = 500;

		ErrorResponseDTO out = new ErrorResponseDTO(getLogTraceInfo(), RestExecutionResultEnum.GENERIC_ERROR.getType(), RestExecutionResultEnum.GENERIC_ERROR.getTitle(), ExceptionUtils.getMessage(ex), status, ErrorInstanceEnum.NO_INFO.getInstance());

    	HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        
    	return new ResponseEntity<>(out, headers, status);
    }
 
	/**
	 * Management validation exception.
	 * 
	 * @param ex		exception
	 * @param request	request
	 * @return			
	 */
    @ExceptionHandler(value = {ValidationPublicationErrorException.class})
    protected ResponseEntity<ErrorResponseDTO> handleValidationException(final ValidationPublicationErrorException ex, final WebRequest request) {
    	log.error("" , ex);  
    	Integer status = 400;
     
    	if (RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.equals(ex.getResult()) || 
    			RestExecutionResultEnum.INVALID_TOKEN_FIELD.equals(ex.getResult())) {
        	status = 403;
    	}
    	
    	ErrorResponseDTO out = new ErrorResponseDTO(getLogTraceInfo(), ex.getResult().getType(), ex.getResult().getTitle(), ex.getMessage(), status, ex.getErrorInstance());
    	
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        
    	return new ResponseEntity<>(out, headers, status);
    }
	
	private LogTraceInfoDTO getLogTraceInfo() {
		return new LogTraceInfoDTO(
				tracer.currentSpan().context().spanIdString(), 
				tracer.currentSpan().context().traceIdString());
	}
	
}