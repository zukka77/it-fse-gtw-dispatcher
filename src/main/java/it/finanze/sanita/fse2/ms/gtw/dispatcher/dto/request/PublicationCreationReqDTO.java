package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request;

import java.util.List;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.AbstractDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthDataFormatEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthcareFacilityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PracticeSettingCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.TipoDocAltoLivEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author CPIERASC
 *
 *	Request body publication creation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicationCreationReqDTO extends AbstractDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -5437339267676346343L;

	@Schema(description = "Identificativo del workflow instance id", required = true)
	@Size(min = 1, max = 256)
	private String workflowInstanceId;

	@Schema(description = "Formato dei dati sanitari")
	private HealthDataFormatEnum healthDataFormat;

	@Schema(description = "Modalità di iniezione del CDA")
	private InjectionModeEnum mode;

	@Schema(description = "Tipologia struttura che ha prodotto il documento", required = true)
	private HealthcareFacilityEnum tipologiaStruttura;

	@Schema(description = "Regole di accesso")
	@Size(min = 0, max = 100)
	private List<EventCodeEnum> regoleAccesso;

	@Schema(description = "Identificativo documento", required = true)
	@Size(min = 0, max = 100)
	private String identificativoDoc;

	@Schema(description = "Identificativo repository", required = true)
	@Size(min = 0, max = 100)
	private String identificativoRep;

	@Schema(description = "Tipo documento alto livello",required = true)
	private TipoDocAltoLivEnum tipoDocumentoLivAlto;

	@Schema(description = "Assetto organizzativo che ha portato alla creazione del documento", required = true)
	private PracticeSettingCodeEnum assettoOrganizzativo;

	@Schema(description = "Identificativo del paziente al momento della creazione del documento", required = true)
	@Size(min = 11, max = 16)
	private String identificativoPaziente;

	@Schema(description = "Data inizio prestazione")
	@Size(min = 0, max = 100)
	private String dataInizioPrestazione;

	@Schema(description = "Data fine prestazione")
	@Size(min = 0, max = 100)
	private String dataFinePrestazione;

	@Schema(description = "Conservazione sostitutiva")
	@Size(min = 0, max = 100)
	private String conservazioneSostitutiva;

	@Schema(description = "Tipo attività clinica",required = true)
	private AttivitaClinicaEnum tipoAttivitaClinica;

	@Schema(description = "Identificativo sottomissione",required = true)
	@Size(min = 0, max = 100)
	private String identificativoSottomissione;

    @Schema(description = "Forza pubblicazione documento" , defaultValue = "false")
    private boolean forcePublish;

}
