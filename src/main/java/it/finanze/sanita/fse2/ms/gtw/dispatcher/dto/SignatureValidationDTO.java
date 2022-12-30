package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignatureValidationDTO {
	
	private Boolean status;
	private List<SignatureInfoDTO> signatures;
	
	public Integer getNumSignatures() {
		Integer out = 0;
		if (signatures!=null) {
			out = signatures.size();
		}
		return out;
	}
}