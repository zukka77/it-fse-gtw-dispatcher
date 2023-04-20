package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.SignCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.SignatureValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.CheckValidationSignEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ErrorInstanceEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.ISignSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.SignerUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;

@Service
public class SignSRV implements ISignSRV{

	@Autowired
	private SignCFG signCFG;

	@Override
	public String checkPades(final byte[] pdf,final EventTypeEnum eventTypeEnum) {
		String out = "";
		
		if(CheckValidationSignEnum.DISABLED.equals(signCFG.getSignValidationType())){
			return out;
		}
 
		boolean checkIsSigned = SignerUtility.isSigned(pdf);
		if(!checkIsSigned) {
			out = "Il pdf non risulta firmato";
		}

		SignatureValidationDTO esitoSign = SignerUtility.validate(pdf);
		if(StringUtility.isNullOrEmpty(out) && Boolean.FALSE.equals(esitoSign.getStatus())) {
			out = "La firma del pdf non risulta valida";
		}
		
		if(!StringUtility.isNullOrEmpty(out) && CheckValidationSignEnum.ERROR.equals(signCFG.getSignValidationType())) {
			ErrorResponseDTO error = ErrorResponseDTO.builder()
					.type(ErrorInstanceEnum.SIGN_EXCEPTION.getInstance())
					.detail(out)
					.instance(ErrorInstanceEnum.SIGN_EXCEPTION.getInstance())
					.title(ErrorInstanceEnum.SIGN_EXCEPTION.getInstance()).build();
			throw new ValidationException(error);
		}
		
		return out;
	}

}
