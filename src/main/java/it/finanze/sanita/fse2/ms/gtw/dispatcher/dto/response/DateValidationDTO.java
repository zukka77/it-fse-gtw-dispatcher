package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DateValidationDTO extends ResponseDTO { 

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -1550024371939901939L;
	
	@Size(min = 0, max = 10000)
	@Schema(description = "Dettaglio del warning")
	private String objectId;
		

	public DateValidationDTO(final LogTraceInfoDTO traceInfo, String inObjectId) {
		super(traceInfo);
		objectId = inObjectId;
	}
	
}
