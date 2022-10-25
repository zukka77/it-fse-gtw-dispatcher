package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO that represents the response of the who-is api.
 * 
 * @author Simone Lungarella
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WhoIsResponseDTO {

    /**
     * Gateway name.
     */
    @Schema(minLength = 1, maxLength = 50, description = "Gateway name", example = "FSE2-GTW-01")
    private String gatewayName;
}
