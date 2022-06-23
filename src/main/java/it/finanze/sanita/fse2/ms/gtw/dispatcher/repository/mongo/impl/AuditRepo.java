package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.impl;

import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IAuditRepo;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 *	@author vincenzoingenito
 *
 */
@Repository
@Slf4j
public class AuditRepo implements IAuditRepo {

	private static final String COLLECTION_NAME = "audit";
	
	@Autowired
	private MongoTemplate mongoTemplate;
 
	/**
	 * Salvataggio audit request and response.
	 */
	@Override 
	public void save(final Map<String, Object> auditMap) {
		try { 
			Document doc = new Document(auditMap);
			mongoTemplate.insert(doc, COLLECTION_NAME);
		} catch (final Exception ex) {
			log.error("Errore durante il salvataggio dell'audit", ex);
			throw new BusinessException("Errore durante il salvataggio dell'audit", ex);
		}
	}
	 
	
}