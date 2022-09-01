package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.OperationalContextEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RegionCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RoleEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author CPIERASC
 * 
 * 	Metadata user INI.
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class MetadataUserDTO extends AbstractDTO {

	@Schema(description = "Identificativo")
	private final String identificativo;

	@Schema(description = "Ruolo")
	private final RoleEnum ruolo;

	@Schema(description = "Struttura")
	private final String struttura;

	@Schema(description = "Identificativo organizzazione")
	private final RegionCodeEnum idOrganizzazione;

	@Schema(description = "Contesto operativo")
	private final OperationalContextEnum contestoOperativo;

}
