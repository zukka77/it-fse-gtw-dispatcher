/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 	Metadata Patient.
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class MetadataPatientDTO extends AbstractDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 7071919094866301049L;

	@Schema(description = "Identificativo")
	private final String identificativo;

	@Schema(description = "Flag presa in carico")
	private final Boolean flagPresaInCarico;
	
}
