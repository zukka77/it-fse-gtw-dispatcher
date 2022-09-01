package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RoleEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author CPIERASC
 * 
 * Author metadata.
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class AuthorDTO extends AbstractDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 7225899029789423608L;

	@Schema(description = "Istituzione")
	private final String Istitution;

	@Schema(description = "Persona")
	private final String person;

	@Schema(description = "Ruolo")
	private final RoleEnum ruolo;

}
