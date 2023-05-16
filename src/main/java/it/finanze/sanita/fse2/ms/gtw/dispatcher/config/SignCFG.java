package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.CheckValidationSignEnum;
import lombok.Getter;

@Configuration
@Getter
public class SignCFG {

	@Value("${sign.choose-validation-type}")
	private CheckValidationSignEnum signValidationType;
	
	@Value("${jwt.ts.issuer}")
	private String tsIssuer;

}
