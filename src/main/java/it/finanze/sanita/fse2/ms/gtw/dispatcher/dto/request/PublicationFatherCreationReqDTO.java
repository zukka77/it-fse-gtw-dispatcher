/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthDataFormatEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *	Request body publication creation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublicationFatherCreationReqDTO extends PublicationMetadataReqDTO {

	@Schema(description = "Formato dei dati sanitari")
	private HealthDataFormatEnum healthDataFormat;

	@Schema(description = "Modalità di iniezione del CDA")
	private InjectionModeEnum mode;

	@Schema(description = "Identificativo documento", required = true)
	@Size(min = 0, max = 100)
	private String identificativoDoc;

	@Schema(description = "Identificativo repository", required = true)
	@Size(min = 0, max = 100)
	private String identificativoRep;

    @Schema(description = "Priorita")
    private Boolean priorita;
    
   
}
