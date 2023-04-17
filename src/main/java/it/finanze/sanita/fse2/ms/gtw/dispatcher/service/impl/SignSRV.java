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

@Service
public class SignSRV implements ISignSRV{

	@Autowired
	private SignCFG signCFG;

	@Override
	public void checkPades(final byte[] pdf,final EventTypeEnum eventTypeEnum) {

		if(CheckValidationSignEnum.DISABLED.equals(signCFG.getSignValidationType())){
			return;
		}

		if(CheckValidationSignEnum.PARTIAL.equals(signCFG.getSignValidationType()) && (EventTypeEnum.VALIDATION_FOR_PUBLICATION.equals(eventTypeEnum) || 
				EventTypeEnum.VALIDATION_FOR_REPLACE.equals(eventTypeEnum))) {
			return;
		}


		boolean checkIsSigned = SignerUtility.isSigned(pdf);
		if(!checkIsSigned) {
			ErrorResponseDTO error = ErrorResponseDTO.builder()
					.type(ErrorInstanceEnum.EMPTY_SIGN_EXCEPTION.getInstance())
					.detail(ErrorInstanceEnum.EMPTY_SIGN_EXCEPTION.getDescription())
					.instance(ErrorInstanceEnum.EMPTY_SIGN_EXCEPTION.getInstance())
					.title(ErrorInstanceEnum.EMPTY_SIGN_EXCEPTION.getDescription())
					.build();
			throw new ValidationException(error);
		}

		SignatureValidationDTO esitoSign = SignerUtility.validate(pdf);
		if(Boolean.FALSE.equals(esitoSign.getStatus())) {
			ErrorResponseDTO error = ErrorResponseDTO.builder()
					.type(ErrorInstanceEnum.EMPTY_SIGN_EXCEPTION.getInstance())
					.detail(ErrorInstanceEnum.WRONG_SIGN_EXCEPTION.getDescription())
					.instance(ErrorInstanceEnum.WRONG_SIGN_EXCEPTION.getInstance())
					.title(ErrorInstanceEnum.WRONG_SIGN_EXCEPTION.getInstance()).build();
			throw new ValidationException(error);
		}


	}

}
