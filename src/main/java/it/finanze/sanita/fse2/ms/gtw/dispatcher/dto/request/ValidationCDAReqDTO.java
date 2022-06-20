package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request;

import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.AbstractDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.HealthDataFormatEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

/**
 * 
 * @author CPIERASC
 *
 *	Request body validazione CDA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@Builder
@JacksonStdImpl
public class ValidationCDAReqDTO extends AbstractDTO {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -5495396011147827913L;

    @Schema(description = "Formato dei dati sanitari")
    private HealthDataFormatEnum healthDataFormat;
    
    @Schema(description = "Modalità di iniezione del CDA")
    private InjectionModeEnum mode;

    @Schema(description = "Attività del gateway", required = true)
    private ActivityEnum activity;
               
}
