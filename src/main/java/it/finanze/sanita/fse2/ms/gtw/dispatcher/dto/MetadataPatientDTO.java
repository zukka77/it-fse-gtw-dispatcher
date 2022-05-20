package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * @author CPIERASC
 * 
 * 	Metadata Patient.
 */
@Data
public class MetadataPatientDTO extends AbstractDTO {

	@Schema(description = "Identificativo")
	private final String identificativo;

	@Schema(description = "Flag presa in carico")
	private final Boolean flagPresaInCarico;
	
}
