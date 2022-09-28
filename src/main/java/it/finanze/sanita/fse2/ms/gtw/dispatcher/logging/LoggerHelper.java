package it.finanze.sanita.fse2.ms.gtw.dispatcher.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.LogDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ILogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/** 
 * 
 * @author vincenzoingenito 
 */ 
@Service
@Slf4j
public class LoggerHelper {
    
	Logger kafkaLog = LoggerFactory.getLogger("kafka-logger"); 
	
	@Value("${log.kafka-log.enable}")
	private boolean kafkaLogEnable;
	
	/* 
	 * Specify here the format for the dates 
	 */
	private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS"); 
	
	
	/* 
	 * Implements structured logs, at all logging levels
	 */
	public void trace(String message, ILogEnum operation, 
			   ResultLogEnum result, Date startDateOperation, String issuer) {
		
		LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				build();

		final String logMessage = StringUtility.toJSON(logDTO);
		log.trace(logMessage);

		log.info("Kafka log enable : " + kafkaLogEnable);
		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.trace(logMessage);
		}
	} 
	
	public void debug(String message,  ILogEnum operation,  
			   ResultLogEnum result, Date startDateOperation, String issuer) {
		
		LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				build();
		
		final String logMessage = StringUtility.toJSON(logDTO);
		log.debug(logMessage);
		log.info("Kafka log enable : " + kafkaLogEnable);
		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.debug(logMessage);
		}
	} 
	 
	public void info(String message, ILogEnum operation, ResultLogEnum result, 
		Date startDateOperation, String issuer, String documentType) {
		
		LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				op_document_type(documentType).
				build();
		
		final String logMessage = StringUtility.toJSON(logDTO);
		log.info(logMessage);
		log.info("Kafka log enable : " + kafkaLogEnable);
		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.info(logMessage);
			log.info("After send kafka message");
		}
	} 
	
	public void warn(String message, ILogEnum operation,  
			   ResultLogEnum result, Date startDateOperation, String issuer) {
		
		LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				build();
		
		final String logMessage = StringUtility.toJSON(logDTO);
		log.warn(logMessage);
		log.info("Kafka log enable : " + kafkaLogEnable);
		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.warn(logMessage);
		}
 
	} 
	
	public void error(String message, ILogEnum operation,  
			   ResultLogEnum result, Date startDateOperation,
			   ILogEnum error, String issuer, String documentType) {
		
		LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				op_error(error.getCode()).
				op_error_description(error.getDescription()).
				op_document_type(documentType).
				build();
		
		final String logMessage = StringUtility.toJSON(logDTO);
		log.error(logMessage);
		log.info("Kafka log enable : " + kafkaLogEnable);
		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.error(logMessage);
		}
		
	}
    	
    
}
