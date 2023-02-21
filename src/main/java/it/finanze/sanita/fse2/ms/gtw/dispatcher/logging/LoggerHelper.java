/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.LogDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ILogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;
 
@Service
@Slf4j
public class LoggerHelper {
    
	Logger kafkaLog = LoggerFactory.getLogger("kafka-logger"); 
	
    @Autowired
	private IConfigClient configClient;
	
	private String gatewayName;
	
	@Value("${log.kafka-log.enable}")
	private boolean kafkaLogEnable;

	@Value("${spring.application.name}")
	private String msName;
	
	/* 
	 * Specify here the format for the dates 
	 */
	private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS"); 
	
	/* 
	 * Implements structured logs, at all logging levels
	 */
	public void trace(String logType,String workflowInstanceId, String message, ILogEnum operation, ResultLogEnum result, Date startDateOperation, 
			String issuer, String role, String subjectFiscalCode, String locality,
			String applicationId, String applicationVendor, String applicationVersion) {

		LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				op_locality(locality).
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				op_role(role).
				op_fiscal_code(subjectFiscalCode).
				gateway_name(getGatewayName()).
				microservice_name(msName).
				op_application_id(applicationId).
				op_application_vendor(applicationVendor).
				op_application_version(applicationVersion).
				log_type(logType).
				workflow_instance_id(workflowInstanceId).
				build();

		final String logMessage = StringUtility.toJSON(logDTO);
		log.trace(logMessage);

		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.trace(logMessage);
		}
	}

	public void debug(String logType,String workflowInstanceId, String message,  ILogEnum operation, ResultLogEnum result, Date startDateOperation, 
				String issuer, String role, String subject, String locality,
				String applicationId, String applicationVendor, String applicationVersion) {
		
		LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				op_locality(locality).
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				op_role(role).
				op_fiscal_code(subject).
				gateway_name(getGatewayName()).
				microservice_name(msName).
				op_application_id(applicationId).
				op_application_vendor(applicationVendor).
				op_application_version(applicationVersion).
				log_type(logType).
				workflow_instance_id(workflowInstanceId).
				build();
		
		final String logMessage = StringUtility.toJSON(logDTO);
		log.debug(logMessage);
		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.debug(logMessage);
		}
	} 
	 
	public void info(String logType,String workflowInstanceId, String message, ILogEnum operation, ResultLogEnum result, Date startDateOperation, String issuer, 
			String documentType, String subject, JWTPayloadDTO jwtPayloadDTO) {

		LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				op_locality(jwtPayloadDTO.getLocality()).
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				op_document_type(documentType).
				op_role(jwtPayloadDTO.getSubject_role()).
				op_fiscal_code(subject).
				gateway_name(getGatewayName()).
				microservice_name(msName).
				op_application_id(jwtPayloadDTO.getSubject_application_id()).
				op_application_vendor(jwtPayloadDTO.getSubject_application_vendor()).
				op_application_version(jwtPayloadDTO.getSubject_application_version()).
				log_type(logType).
				workflow_instance_id(workflowInstanceId).
				build();
		
		final String logMessage = StringUtility.toJSON(logDTO);
		log.info(logMessage);
		
		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.info(logMessage);
		}
	} 
	
	public void warn(String logType,String workflowInstanceId, String message, ILogEnum operation, ResultLogEnum result, Date startDateOperation, 
			String issuer, String role, String subject, String locality,
			String applicationId, String applicationVendor, String applicationVersion) {
		
		LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				op_locality(locality).
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				op_role(role).
				op_fiscal_code(subject).
				gateway_name(getGatewayName()).
				microservice_name(msName).
				op_application_id(applicationId).
				op_application_vendor(applicationVendor).
				op_application_version(applicationVersion).
				log_type(logType).
				workflow_instance_id(workflowInstanceId).
				build();
		
		final String logMessage = StringUtility.toJSON(logDTO);
		log.warn(logMessage);
		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.warn(logMessage);
		}
 
	} 
	
	public void error(String logType,String workflowInstanceId, String message, ILogEnum operation, ResultLogEnum result, Date startDateOperation,
			   ILogEnum error, String issuer, String documentType, String role, String subject,
			   JWTPayloadDTO jwtPayloadToken) {
		
		LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				op_locality(jwtPayloadToken.getLocality()).
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				op_error(error.getCode()).
				op_error_description(error.getDescription()).
				op_document_type(documentType).
				op_role(role).
				op_fiscal_code(subject).
				gateway_name(getGatewayName()).
				microservice_name(msName).
				op_application_id(jwtPayloadToken.getSubject_application_id()).
				op_application_vendor(jwtPayloadToken.getSubject_application_vendor()).
				op_application_version(jwtPayloadToken.getSubject_application_version()).
				log_type(logType).
				workflow_instance_id(workflowInstanceId).
				build();
		
		final String logMessage = StringUtility.toJSON(logDTO);
		log.error(logMessage);
		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.error(logMessage);
		}
		
	}

	/**
	 * Returns the gateway name.
	 * 
	 * @return The GatewayName of the ecosystem.
	 */
	private String getGatewayName() {
		if (gatewayName == null) {
			gatewayName = configClient.getGatewayName();
		}
		return gatewayName;
	}
	
}
