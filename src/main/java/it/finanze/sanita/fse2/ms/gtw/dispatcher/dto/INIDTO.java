/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author CPIERASC
 * 
 * 	Metadata INI.
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class INIDTO extends AbstractDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -4974470887829412913L;

	@Schema(description = "Metadati utente")
	private final MetadataUserDTO utente;

	@Schema(description = "Metadati assistito")
	private final MetadataPatientDTO assistito;

	@Schema(description = "Metadati documento")
	private final MetadataDocumentDTO documento;
	
}
