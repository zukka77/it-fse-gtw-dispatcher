package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class PublicationInfoDTO extends AbstractDTO {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -5485396011097827913L;

	@Schema(description = "Identificativo del workflow")
    private String workflowInstanceId;

    @Schema(description = "Identificativo documento")
    private String identificativoDoc;

    @Schema(description = "Identificativo del paziente al momento della creazione del documento")
    private String identificativoPaziente;

    @Schema(description = "Identificativo sottomissione")
    private String identificativoSottomissione;

    @Schema(description = "Ducument publication forced")
    private Boolean forcePublish;

}
