/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
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

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -1781875082884079035L;

	@Autowired
	private transient IAuditRepo auditServiceRepo;


	/*
	 * Metodo di utility per l'audit dei servizi. 
	 */
	@Override
	public void saveAuditReqRes(HttpServletRequest httpServletRequest,Object body) {
		try {
			String[] requestBody = httpServletRequest.getParameterMap().get("requestBody");
 
			if(requestBody!=null) {
				Map<String, Object> auditMap = new HashMap<>();
				auditMap.put("servizio", httpServletRequest.getRequestURI());
				auditMap.put("start_time", httpServletRequest.getAttribute("START_TIME"));
				auditMap.put("end_time", new Date());
				auditMap.put("request", StringUtility.fromJSON(requestBody[0], Object.class));
				auditMap.put("response", body);
				auditMap.put("jwt_issuer", httpServletRequest.getAttribute("JWT_ISSUER"));
				httpServletRequest.removeAttribute("JWT_ISSUER");

				auditServiceRepo.save(auditMap);
			}

		} catch(Exception ex) {
			log.error("Errore nel salvataggio dell'audit : ", ex);
			throw new BusinessException("Errore nel salvataggio dell'audit : ", ex);
		}

	}
 
}