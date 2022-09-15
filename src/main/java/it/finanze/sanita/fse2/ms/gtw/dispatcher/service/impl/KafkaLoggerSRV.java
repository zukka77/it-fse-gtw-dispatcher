package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.LogDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ILogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.ILogSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/** 
 * @author: vincenzoingenito 
 */ 
@Service
@Slf4j
public class KafkaLoggerSRV implements ILogSRV {

	/* 
	 * Specify here the format for the dates 
	 */
	private final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
	
	@Autowired
	private IKafkaSRV kafkaSRV;
	
	/* 
	 * Implements structured logs, at all logging levels
	 */
	@Override
	public void trace(final String message, final ILogEnum operation, final ResultLogEnum result, final Date startDateOperation, 
		final String issuer) {

		final LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				build();
		
		String json = StringUtility.toJSON(logDTO);
		log.trace(json);
		kafkaSRV.sendLoggerStatus(json);

	} 

	@Override
	public void debug(final String message,  final ILogEnum operation, final ResultLogEnum result, 
		final Date startDateOperation, final String issuer) {

		final LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				build();

		String json = StringUtility.toJSON(logDTO);
		log.debug(json);
		kafkaSRV.sendLoggerStatus(json);
	} 

	@Override
	public void info(final String message, final ILogEnum operation, final ResultLogEnum result, 
		final Date startDateOperation, final String issuer) {

		final LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				build();

		String json = StringUtility.toJSON(logDTO);
		log.info(json);
		kafkaSRV.sendLoggerStatus(json);
	} 

	@Override
	public void warn(final String message, final ILogEnum operation, final ResultLogEnum result, final Date startDateOperation, 
		final String issuer) {

		final LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				build();

		String json = StringUtility.toJSON(logDTO);
		log.warn(json);
		kafkaSRV.sendLoggerStatus(json);
	} 

	@Override
	public void error(final String message, final ILogEnum operation, final ResultLogEnum result, final Date startDateOperation,
			final ILogEnum error, final String issuer) {

		final LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				op_error(error.getCode()).
				op_error_description(error.getDescription()).
				build();

		String json = StringUtility.toJSON(logDTO);
		log.error(json);
		kafkaSRV.sendLoggerStatus(json);
	}

}
