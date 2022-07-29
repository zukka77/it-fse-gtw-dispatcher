package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JWTTokenDTO {
    
    private JWTPayloadDTO payload;
}
