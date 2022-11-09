package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteResDTO extends ResponseDTO {

    private final String workflowInstanceId;

    public DeleteResDTO(String workflowInstanceId, LogTraceInfoDTO info) {
        super(info);
        this.workflowInstanceId = workflowInstanceId;
    }

}
