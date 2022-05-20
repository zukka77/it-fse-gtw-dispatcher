package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import java.io.Serializable;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;

import brave.Tracer;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author CPIERASC
 *
 *	Abstract controller.
 */
@Slf4j
public abstract class AbstractCTL implements Serializable {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -3077780100650268134L;
	
	@Autowired
	private Tracer tracer;

	protected LogTraceInfoDTO getLogTraceInfo() {
		LogTraceInfoDTO out = new LogTraceInfoDTO(null, null);
		if (tracer.currentSpan() != null) {
			out = new LogTraceInfoDTO(
					tracer.currentSpan().context().spanIdString(), 
					tracer.currentSpan().context().traceIdString());
		}
		return out;
	}

	protected ValidationCDAReqDTO getValidationJSONObject(String jsonREQ) {
		ValidationCDAReqDTO out = null;
		if(!StringUtility.isNullOrEmpty(jsonREQ)) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				out = mapper.readValue(jsonREQ, ValidationCDAReqDTO.class);
			} catch (Exception ex) {
				log.error("Errore gestione json :" , ex);
			}
		}
		return out;
	}

	protected PublicationCreationReqDTO getPublicationJSONObject(String jsonREQ) {
        PublicationCreationReqDTO out = null;
        if(!StringUtility.isNullOrEmpty(jsonREQ)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                out = mapper.readValue(jsonREQ, PublicationCreationReqDTO.class);
            } catch (Exception ex) {
                log.error("Errore gestione json :" , ex);
            }
        }
        return out;
    }

	protected ValidationCDAReqDTO constructValidationDTO(PublicationCreationReqDTO publicationDTO, ActivityEnum activity) {

        return ValidationCDAReqDTO.builder()
        .healthDataFormat(publicationDTO.getHealthDataFormat())
        .mode(publicationDTO.getMode())
        .activity(activity)
        .tipologiaStruttura(publicationDTO.getTipologiaStruttura())
        .regoleAccesso(publicationDTO.getRegoleAccesso())
        .identificativoDoc(publicationDTO.getIdentificativoDoc())
        .identificativoRep(publicationDTO.getIdentificativoRep())
        .tipoDocumentoLivAlto(publicationDTO.getTipoDocumentoLivAlto())
        .assettoOrganizzativo(publicationDTO.getAssettoOrganizzativo())
        .identificativoPaziente(publicationDTO.getIdentificativoPaziente())
        .dataInizioPrestazione(publicationDTO.getDataInizioPrestazione())
        .dataFinePrestazione(publicationDTO.getDataFinePrestazione())
        .conservazioneSostitutiva(publicationDTO.getConservazioneSostitutiva())
        .tipoAttivitaClinica(publicationDTO.getTipoAttivitaClinica())
        .identificativoSottomissione(publicationDTO.getIdentificativoSottomissione())
        .build();

    }

}
