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
 * 	Metadata Patient.
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class MetadataPatientDTO extends AbstractDTO {

	@Schema(description = "Identificativo")
	private final String identificativo;

	@Schema(description = "Flag presa in carico")
	private final Boolean flagPresaInCarico;
	
}
