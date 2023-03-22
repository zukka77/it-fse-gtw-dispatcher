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
import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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

	@Autowired
	private WebEndpointProperties endpoints;

	@Autowired
	private SwaggerUiConfigProperties swagger;

	@Autowired
	private SpringDocConfigProperties api;

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
			// Check if audit should be skipped
			if(!skip(service)) {
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
			} else {
				log.debug("Skipping audit on path: {}", service);
			}
		} catch(Exception ex) {
			log.error("Errore nel salvataggio dell'audit : ", ex);
			throw new BusinessException("Errore nel salvataggio dell'audit : ", ex);
		}
	}

	private boolean skip(String uri) {
		return skipActuator(uri) || skipSwagger(uri);
	}

	private boolean skipActuator(String uri) {
		boolean skip = false;
		// Skip check if uri is null
		if(uri != null) {
			// Retrieve actuator exposed endpoints
			Set<String> ep = endpoints.getExposure().getInclude();
			// Retrieve mapping
			Map<String, String> mapping = endpoints.getPathMapping();
			// Iterate
			Iterator<String> iterator = ep.iterator();
			// Until we find match
			while (iterator.hasNext() && !skip) {
				// Get value
				String endpoint = iterator.next();
				// Retrieve associated mapping
				// because it may have been re-defined (e.g live -> status ...)
				// If it wasn't overwritten, it will return null therefore we are using the default mapping value
				String path = mapping.getOrDefault(endpoint, endpoint);
				// If path match, exit loop
				if (uri.contains(path)) skip = true;
			}
		}
		return skip;
	}

	private boolean skipSwagger(String uri) {
		boolean skip = false;
		// Skip check if uri is null
		if(uri != null) {
			// Swagger page
			String ui = swagger.getPath();
			// Generative API
			String docs = api.getApiDocs().getPath();
			// Retrieve swagger-ui exposed endpoints
			skip = uri.contains(ui) || uri.contains(docs);
		}
		return skip;
	}

}