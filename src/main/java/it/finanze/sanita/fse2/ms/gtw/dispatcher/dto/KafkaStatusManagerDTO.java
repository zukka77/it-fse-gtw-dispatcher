/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import java.util.Date;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventTypeEnum;
import lombok.Builder;
import lombok.Getter;

/**
 * Content of a Kafka message
 */
@Getter
@Builder
public class KafkaStatusManagerDTO extends AbstractDTO {

	private String traceId;
	
	private EventTypeEnum eventType;
	
	private Date eventDate;
	
	private EventStatusEnum eventStatus;
	
	private String message;
	
	private String identificativoDocumento;
	
	private String subject;
	
	private String subjectRole;
	
	private AttivitaClinicaEnum tipoAttivita;
	
	private String organizzazione;
	
	private String issuer;
	
	private String microserviceName;
	
}
