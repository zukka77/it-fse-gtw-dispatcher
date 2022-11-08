/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AssettoOrgEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthcareFacilityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.LowLevelDocEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.TipoDocAltoLivEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 	Metadata Document.
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class MetadataDocumentDTO extends AbstractDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 6130637113806503422L;

	@Schema(description = "Tipo livello alto")
	private TipoDocAltoLivEnum tipoLivAlto;

	@Schema(description = "Tipo livello basso")
	private LowLevelDocEnum tipoLivBasso;

	@Schema(description = "Autore")
	private AuthorDTO autore;

	@Schema(description = "Identificativo")
	private String identificativo;

	@Schema(description = "Data prestazione")
	private Date dataPrestazione;

	@Schema(description = "Lista riferimenti")
	private List<String> listaRiferimenti;

	@Schema(description = "Tipo struttura")
	private HealthcareFacilityEnum tipoStruttura;

	@Schema(description = "Lista codice eventi")
	private List<String> listaCodiceEventi;

	@Schema(description = "Flag conservazione sostitutiva")
	private Boolean flagConsSostitutiva;

	@Schema(description = "Assetto organizzazione")
	private AssettoOrgEnum assettoOrg;

	@Schema(description = "Tipo attività clinica")
	private AttivitaClinicaEnum tipoAttivitaClinica;

	@Schema(description = "Identificativo sottomissione")
	private String idSottomissione;

	@Schema(description = "Identificativo Organizzazione custode")
	private String idOrganizzazioneCustode;

	@Schema(description = "Identificativo repository")
	private String idRepository;

}
