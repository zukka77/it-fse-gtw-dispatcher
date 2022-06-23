package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import brave.Tracer;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ValidationErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ValidationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationErrorException;
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
    	if (ValidationResultEnum.SEMANTIC_ERROR.equals(ex.getResult())) {
        	status = 422;
    	}
    	
    	if (ValidationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.equals(ex.getResult())) {
        	status = 401;
    	}
    	
    	if(ValidationResultEnum.DOCUMENT_TYPE_ERROR.equals(ex.getResult())) {
    		status = 415;
    	}

    	String workflowInstanceId = StringUtility.isNullOrEmpty(ex.getWorkflowInstanceId()) ? null : ex.getWorkflowInstanceId(); 
    	
    	ValidationErrorResponseDTO out = new ValidationErrorResponseDTO(getLogTraceInfo(), ex.getResult().getType(), ex.getResult().getTitle(), ex.getMessage(), status, ex.getResult().getType(), workflowInstanceId);
    	
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
    @ExceptionHandler(value = {ConnectionRefusedException.class})
    protected ResponseEntity<ErrorResponseDTO> handleConnectionRefusedException(final ConnectionRefusedException ex, final WebRequest request) {
    	log.error("" , ex);
    	Integer status = 500;
    	
    	String url  = ex.getUrl() != null ? ex.getUrl() : "";
    	String detailedMessage = ex.getMessage() + " " + url; 
    	ErrorResponseDTO out = new ErrorResponseDTO(getLogTraceInfo(), "/msg/connection-refused", ex.getMessage(), detailedMessage, status, "/msg/connection-refused");

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
    	log.error("" , ex);
    	Integer status = 500;

		ErrorResponseDTO out = new ErrorResponseDTO(getLogTraceInfo(), "/msg/generic-error", "Errore generico", "Errore generico", status, "/msg/generic-error");

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
    @ExceptionHandler(value = {MaxUploadSizeExceededException.class})
    protected ResponseEntity<ErrorResponseDTO> handleMaxUploadSizeExceededException(final Exception ex, final WebRequest request) {
    	log.error("" , ex);
    	ValidationResultEnum result = ValidationResultEnum.DOCUMENT_SIZE_ERROR;
    	Integer status = 400;

		ErrorResponseDTO out = new ErrorResponseDTO(getLogTraceInfo(), result.getType(), result.getTitle(), result.getTitle(), status, result.getTitle());

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
     
    	if (PublicationResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.equals(ex.getResult())) {
        	status = 401;
    	}
    	ErrorResponseDTO out = new ErrorResponseDTO(getLogTraceInfo(), ex.getResult().getType(), ex.getResult().getTitle(), ex.getMessage(), status, ex.getResult().getType());
    	
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