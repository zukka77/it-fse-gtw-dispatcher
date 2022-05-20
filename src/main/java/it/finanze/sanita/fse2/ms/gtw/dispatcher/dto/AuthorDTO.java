package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RoleEnum;
import lombok.Data;


/**
 * @author CPIERASC
 * 
 * Author metadata.
 */
@Data
public class AuthorDTO extends AbstractDTO {

	@Schema(description = "Istituzione")
	private final String Istitution;

	@Schema(description = "Persona")
	private final String person;

	@Schema(description = "Ruolo")
	private final RoleEnum ruolo;

}
