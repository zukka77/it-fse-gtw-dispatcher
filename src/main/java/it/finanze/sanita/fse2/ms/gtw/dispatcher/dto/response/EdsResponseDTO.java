package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.AbstractDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class EdsResponseDTO extends AbstractDTO {

	private Boolean esito;

	private String errorMessage;

}
