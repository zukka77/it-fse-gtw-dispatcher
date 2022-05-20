package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * @author CPIERASC
 * 
 * 	Metadata INI.
 */
@Data
public class INIDTO extends AbstractDTO {

	@Schema(description = "Metadati utente")
	private final MetadataUserDTO utente;

	@Schema(description = "Metadati assistito")
	private final MetadataPatientDTO assistito;

	@Schema(description = "Metadati documento")
	private final MetadataDocumentDTO documento;
	
}
