/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LogDTO {

	final String log_type = "gateway-structured-log";
	
	private String message;
	
	private String operation;
	
	private String op_result;
	
	private String op_timestamp_start;
	
	private String op_timestamp_end;
	
	private String op_error;
	
	private String op_error_description;
	
	private String op_issuer;

	private String op_locality;

	private String op_document_type;
	
	private String op_role;

	private String op_fiscal_code;

	private String gateway_name;
	
	private String microservice_name;
	
	private String op_application_id;
	
	private String op_application_vendor;
	
	private String op_application_version;
}
