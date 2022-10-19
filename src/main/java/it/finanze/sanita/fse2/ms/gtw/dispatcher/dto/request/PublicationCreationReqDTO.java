/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request;

import java.util.List;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
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
public class PublicationCreationReqDTO extends PublicationMetadataReqDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -5437339267676346343L;

	@Schema(description = "Identificativo del workflow")
	@Size(min = 0, max = 256)
	private String workflowInstanceId;

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

    @Schema(description = "Forza pubblicazione documento" , defaultValue = "false")
    @Hidden
    private boolean forcePublish;

    @Schema(description = "Priorita")
    private Boolean priorita;
    
    @Builder
    public PublicationCreationReqDTO(
    		String workflowInstanceId,
    		HealthDataFormatEnum healthDataFormat,
    		InjectionModeEnum mode,
    		String identificativoDoc,
    		String identificativoRep,
    		boolean forcePublish,
    		Boolean priorita,
    		HealthcareFacilityEnum tipologiaStruttura, 
    		List<String> attiCliniciRegoleAccesso, 
    		TipoDocAltoLivEnum tipoDocumentoLivAlto, 
    		PracticeSettingCodeEnum assettoOrganizzativo, 
    		String dataInizioPrestazione, 
    		String dataFinePrestazione, 
    		String conservazioneANorma,
    		AttivitaClinicaEnum tipoAttivitaClinica,
    		String identificativoSottomissione) {
    	super(tipologiaStruttura, attiCliniciRegoleAccesso, tipoDocumentoLivAlto, assettoOrganizzativo, dataInizioPrestazione, dataFinePrestazione, conservazioneANorma, tipoAttivitaClinica, identificativoSottomissione);
    	this.workflowInstanceId = workflowInstanceId;
    	this.healthDataFormat = healthDataFormat;
    	this.mode = mode;
    	this.identificativoDoc = identificativoDoc;
    	this.identificativoRep = identificativoRep;
    	this.forcePublish = forcePublish;
    	this.priorita = priorita;
    }
    
}
