/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.AuditETY;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IAuditRepo;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IAuditSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * Classe per l'audit dei servizi .
 */
@Component 
@Slf4j
@ConditionalOnProperty("ms.dispatcher.audit.enabled")
public class AuditSRV implements IAuditSRV {


	@Autowired
	private IAuditRepo auditServiceRepo;


	/*
	 * Metodo di utility per l'audit dei servizi. 
	 */
	@Override
	public void saveAuditReqRes(HttpServletRequest httpServletRequest,Object body) {
		try {
			String[] requestBody = httpServletRequest.getParameterMap().get("requestBody");
 
			if(requestBody!=null) {
				AuditETY audit = new AuditETY();
				audit.setServizio(httpServletRequest.getRequestURI());
				audit.setStart_time((Date)httpServletRequest.getAttribute("START_TIME"));
				audit.setEnd_time(new Date());
				audit.setRequest(StringUtility.fromJSON(requestBody[0], Object.class));
				audit.setResponse(body);
				audit.setJwt_issuer((String)httpServletRequest.getAttribute("JWT_ISSUER"));
				audit.setHttpMethod(httpServletRequest.getMethod());
				httpServletRequest.removeAttribute("JWT_ISSUER");
				auditServiceRepo.save(audit);
			} else if(HttpMethod.DELETE.toString().equals(httpServletRequest.getMethod())) {
				AuditETY audit = new AuditETY();
				audit.setServizio(httpServletRequest.getRequestURI());
				audit.setStart_time(new Date());
				audit.setEnd_time(new Date());
				audit.setResponse(body);
				audit.setHttpMethod(httpServletRequest.getMethod());
				auditServiceRepo.save(audit);
			} 
		} catch(Exception ex) {
			log.error("Errore nel salvataggio dell'audit : ", ex);
			throw new BusinessException("Errore nel salvataggio dell'audit : ", ex);
		}
	}
 
}