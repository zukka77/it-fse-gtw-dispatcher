package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IAuditRepo;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IAuditSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author vincenzoingenito
 * 
 * Classe per l'audit dei servizi .
 */
@Component 
@Slf4j
public class AuditSRV implements IAuditSRV {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -1781875082884079035L;

	@Autowired
	private IAuditRepo auditServiceRepo;


	/*
	 * Metodo di utility per l'audit dei servizi. 
	 */
	@Override
	public void saveAuditReqRes(HttpServletRequest httpServletRequest,Object body) {
		try {
			Long auditStart = System.currentTimeMillis();
			String[] requestBody = httpServletRequest.getParameterMap().get("requestBody");
 
			if(requestBody!=null) {
				Map<String, Object> auditMap = new HashMap<>();
				auditMap.put("servizio", httpServletRequest.getRequestURI());
				auditMap.put("start_time", httpServletRequest.getAttribute("START_TIME"));
				auditMap.put("end_time", new Date());
				auditMap.put("request", StringUtility.fromJSON(requestBody[0], Object.class));
				auditMap.put("response", body);
				
				String jwtIssuer = (String)httpServletRequest.getAttribute("JWT_ISSUER");
				if(StringUtility.isNullOrEmpty(jwtIssuer)) {
					jwtIssuer = "ISSUER_NON_PRESENTE";
				}
				auditMap.put("jwt_issuer", jwtIssuer);
				httpServletRequest.removeAttribute("JWT_ISSUER");
				auditServiceRepo.save(auditMap);   
			}
			Long endAudit = System.currentTimeMillis() - auditStart;
			log.info("SAVE AUDIT : " + endAudit + " ms");

		} catch(Exception ex) {
			log.error("Errore nel salvataggio dell'audit : ", ex);
			throw new BusinessException("Errore nel salvataggio dell'audit : ", ex);
		}

	}
 
}