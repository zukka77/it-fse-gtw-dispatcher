package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import java.security.Principal;
import java.util.Date;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignatureInfoDTO {
	
	private Principal principal;
	private Date notAfter;
	private Date notBefore;
	private String fieldType;
	private String fullyQualifiedName;
	private String location;
	private String contactInfo;
	private String name;
	private String reason;
	private Date signDate;
	private Boolean valid;
	private String digestAlgOID;
	private String encrypAlgOID;
	
}