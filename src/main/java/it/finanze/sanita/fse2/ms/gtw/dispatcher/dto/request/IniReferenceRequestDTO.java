package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IniReferenceRequestDTO {
    private String idDoc;
    private JWTPayloadDTO token;
}
