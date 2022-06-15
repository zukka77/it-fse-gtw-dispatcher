package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class ValidationCDAInfoDTO extends AbstractDTO {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -5488396011147827913L;

	@Schema(description = "Identificativo della transazione")
    private String transactionID;

    @Schema(description = "Attività del gateway")
    private ActivityEnum activity;

    @Schema(description = "Identificativo documento")
    private String identificativoDoc;

    @Schema(description = "Identificativo del paziente al momento della creazione del documento")
    private String identificativoPaziente;

    @Schema(description = "Identificativo sottomissione")
    private String identificativoSottomissione;

}
