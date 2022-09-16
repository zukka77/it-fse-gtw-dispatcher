package it.finanze.sanita.fse2.ms.gtw.dispatcher.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.LogDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ILogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl.KafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;

/** 
 * 
 * @author: Guido Rocco - IBM 
 */ 
@Service
@Slf4j
public class ElasticLoggerHelper {
    
	Logger elasticLog = LoggerFactory.getLogger("elastic-logger"); 
	  
	@Value("${log.elastic-search.enable}")
	private boolean elasticLogEnable;
	
	@Value("${log.kafka-log.enable}")
	private boolean kafkaLogEnable;
	
	/* 
	 * Specify here the format for the dates 
	 */
	private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS"); 
	
	@Autowired
	private KafkaSRV kafkaSRV;
	
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

		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaSRV.sendLoggerStatus(logMessage, operation.getCode());
		}
		
		if(elasticLogEnable) {
			elasticLog.trace(message, 
					StructuredArguments.kv("op-issuer", issuer),
					StructuredArguments.kv("operation", operation.getCode()), 
					StructuredArguments.kv("op-result", result.getCode()),
					StructuredArguments.kv("op-timestamp-start", dateFormat.format(startDateOperation)),
					StructuredArguments.kv("op-timestamp-end", dateFormat.format(new Date()))); 
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

		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaSRV.sendLoggerStatus(logMessage, operation.getCode());
		}

		if(elasticLogEnable) {
			elasticLog.debug(message,  
					StructuredArguments.kv("op-issuer", issuer),
					StructuredArguments.kv("operation", operation.getCode()), 
					StructuredArguments.kv("op-result", result.getCode()),
					StructuredArguments.kv("op-timestamp-start", dateFormat.format(startDateOperation)),
					StructuredArguments.kv("op-timestamp-end", dateFormat.format(new Date()))); 
		}
	} 
	 
	public void info(String message, ILogEnum operation,  
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

		long startTime = System.currentTimeMillis();
		log.info(logMessage);
		long endTime = System.currentTimeMillis();
		log.info("Time to log on console: " + (endTime - startTime) + " ms");

		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			startTime = System.currentTimeMillis();
			kafkaSRV.sendLoggerStatus(logMessage, operation.getCode());
			endTime = System.currentTimeMillis();
			log.info("Time to log on kafka: " + (endTime - startTime) + " ms");
		}

		if(elasticLogEnable) {
			elasticLog.info(message,  
					StructuredArguments.kv("op-issuer", issuer),
					StructuredArguments.kv("operation", operation.getCode()), 
					StructuredArguments.kv("op-result", result.getCode()),
					StructuredArguments.kv("op-timestamp-start", dateFormat.format(startDateOperation)),
					StructuredArguments.kv("op-timestamp-end", dateFormat.format(new Date()))); 
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
		long startTime = System.currentTimeMillis();
		log.warn(logMessage);
		long endTime = System.currentTimeMillis();
		log.info("Time to log on console: " + (endTime - startTime) + " ms");

		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			startTime = System.currentTimeMillis();
			kafkaSRV.sendLoggerStatus(logMessage, operation.getCode());
			endTime = System.currentTimeMillis();
			log.info("Time to log on kafka: " + (endTime - startTime) + " ms");
		}

		if(elasticLogEnable) {
			elasticLog.warn(message, 
					StructuredArguments.kv("op-issuer", issuer),
					StructuredArguments.kv("operation", operation.getCode()), 
					StructuredArguments.kv("op-result", result.getCode()),
					StructuredArguments.kv("op-timestamp-start", dateFormat.format(startDateOperation)),
					StructuredArguments.kv("op-timestamp-end", dateFormat.format(new Date()))); 
		}
	} 
	
	public void error(String message, ILogEnum operation,  
			   ResultLogEnum result, Date startDateOperation,
			   ILogEnum error, String issuer) {
		
		LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				op_error(error.getCode()).
				op_error_description(error.getDescription()).
				build();
		
		final String logMessage = StringUtility.toJSON(logDTO);
		long startTime = System.currentTimeMillis();
		log.error(logMessage);
		long endTime = System.currentTimeMillis();
		log.info("Time to log on console: " + (endTime - startTime) + " ms");

		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			startTime = System.currentTimeMillis();
			kafkaSRV.sendLoggerStatus(logMessage, operation.getCode());
			endTime = System.currentTimeMillis();
			log.info("Time to log on kafka: " + (endTime - startTime) + " ms");
		}

		if(elasticLogEnable) {
			elasticLog.error(message,  
					StructuredArguments.kv("op-issuer", issuer),
					StructuredArguments.kv("operation", operation.getCode()), 
					StructuredArguments.kv("op-result", result.getCode()),
					StructuredArguments.kv("op-timestamp-start", dateFormat.format(startDateOperation)),
					StructuredArguments.kv("op-timestamp-end", dateFormat.format(new Date())),
					StructuredArguments.kv("op-error", error.getCode()),
					StructuredArguments.kv("op-error-description", error.getDescription())); 
		}
		
	}
    	
    
}
