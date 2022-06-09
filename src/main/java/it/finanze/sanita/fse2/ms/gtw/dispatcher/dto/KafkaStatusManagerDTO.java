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

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 7080680277816570116L;
	
	private EventTypeEnum eventType;
	
	private Date eventDate;
	
	private EventStatusEnum eventStatus;
	
	private String message;
	
	private String identificativoDocumento;
	
	private String subject;
	
	private String subjectRole;
	
	private AttivitaClinicaEnum tipoAttivita;
	
	private String organizzazione;
	
}
