/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.AuditETY;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IAuditRepo;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IAuditSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;

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
	public void saveAuditReqRes(HttpServletRequest req, Object body) {
		try {
 			String[] requestBody = req.getParameterMap().get("requestBody");
			String service = req.getRequestURI();
			// Decode URI request (if not null)
			if(service != null) service = URLDecoder.decode(service, UTF_8.name());

			if(req.getAttribute("UPDATE_REQ")!=null) {
 				AuditETY audit = new AuditETY();
				audit.setServizio(service);
				audit.setStart_time(new Date());
				audit.setEnd_time(new Date());
				audit.setRequest(req.getAttribute("UPDATE_REQ"));
				audit.setResponse(body);
				audit.setHttpMethod(req.getMethod());
				auditServiceRepo.save(audit);
 			} else if(requestBody!=null) {
				AuditETY audit = new AuditETY();
				audit.setServizio(service);
				audit.setStart_time((Date)req.getAttribute("START_TIME"));
				audit.setEnd_time(new Date());
				audit.setRequest(StringUtility.fromJSON(requestBody[0], Object.class));
				audit.setResponse(body);
				audit.setJwt_issuer((String)req.getAttribute("JWT_ISSUER"));
				audit.setHttpMethod(req.getMethod());
				req.removeAttribute("JWT_ISSUER");
				auditServiceRepo.save(audit);
			} else if(HttpMethod.DELETE.toString().equals(req.getMethod())) {
				AuditETY audit = new AuditETY();
				audit.setServizio(service);
				audit.setStart_time(new Date());
				audit.setEnd_time(new Date());
				audit.setResponse(body);
				audit.setHttpMethod(req.getMethod());
				auditServiceRepo.save(audit);
			} 
		} catch(Exception ex) {
			log.error("Errore nel salvataggio dell'audit : ", ex);
			throw new BusinessException("Errore nel salvataggio dell'audit : ", ex);
		}
	}
 
}