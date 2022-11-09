package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseWifDTO extends ResponseDTO {

    private final String workflowInstanceId;

    public ResponseWifDTO(String workflowInstanceId, LogTraceInfoDTO info) {
        super(info);
        this.workflowInstanceId = workflowInstanceId;
    }

}
