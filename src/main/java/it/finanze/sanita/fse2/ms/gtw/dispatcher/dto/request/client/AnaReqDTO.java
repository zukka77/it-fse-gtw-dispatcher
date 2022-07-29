package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnaReqDTO {
	
	private String codiceFiscale;
	
	private String descrizione;
}
