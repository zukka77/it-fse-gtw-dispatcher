package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseWifDTO extends ResponseDTO {

	private static final long serialVersionUID = 6778081959922405644L;
	
	@Size(min = 0, max = 10000)
	@Schema(description = "Dettaglio del warning")
	private String warning;
	
	@Size(min = 0, max = 256)
	@Schema(description = "Identificativo del workflow instance id")
    private final String workflowInstanceId;

    public ResponseWifDTO(String workflowInstanceId, LogTraceInfoDTO info, String inWarning) {
        super(info);
        this.warning = inWarning;
        this.workflowInstanceId = workflowInstanceId;
    }

}
