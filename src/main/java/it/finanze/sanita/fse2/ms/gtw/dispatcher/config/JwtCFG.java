package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class JwtCFG {
 
	/**
	 * Claims required.
	 */
	@Value("${jwt.claims-required}")
	private boolean claimsRequired;

	@Value("${jwt.ts.issuer}")
	private String tsIssuer;

}
