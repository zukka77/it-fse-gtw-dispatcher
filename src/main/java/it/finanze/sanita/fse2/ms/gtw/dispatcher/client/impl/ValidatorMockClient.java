package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IValidatorClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RawValidationEnum;

/**
 * Test implementation of Validator Client.
 * 
 * @author CPIERASC
 */
@Component
@Profile(Constants.Profile.TEST)
public class ValidatorMockClient extends AbstractClient implements IValidatorClient {
	
	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 8580649402341145521L;

	@Override
	public ValidationInfoDTO validate(String cda) {
		return ValidationInfoDTO.builder().result(RawValidationEnum.OK).build();
	}
	   
}
