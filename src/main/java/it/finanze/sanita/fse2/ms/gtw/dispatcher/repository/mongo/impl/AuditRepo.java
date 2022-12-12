/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.AuditETY;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IAuditRepo;
import lombok.extern.slf4j.Slf4j;
 
@Repository
@Slf4j
@ConditionalOnProperty("ms.dispatcher.audit.enabled")
public class AuditRepo implements IAuditRepo {


	@Autowired
	private MongoTemplate mongoTemplate;


	/**
	 * Salvataggio audit request and response.
	 */
	@Override 
	public void save(final AuditETY auditETY) {
		try { 
			mongoTemplate.insert(auditETY);
		} catch (final Exception ex) {
			log.error("Errore durante il salvataggio dell'audit", ex);
			throw new BusinessException("Errore durante il salvataggio dell'audit", ex);
		}
	}
	 
	
}