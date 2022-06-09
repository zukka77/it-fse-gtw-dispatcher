package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.redis.ICdaRepo;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.ICdaSRV;
import lombok.extern.slf4j.Slf4j;

/**
 *	@author vincenzoingenito
 *
 *	Cda service implementation.
 */
@Service
@Slf4j
public class CdaSRV implements ICdaSRV {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -1000397559663801763L;
	
	@Autowired
	private ICdaRepo cdaRepo;
	
	@Override
	public void create(final String txID, final String hashedCDA) {
		try {
			cdaRepo.create(txID, hashedCDA);
		} catch(Exception ex) {
			log.error("Error creating cda :" ,ex);
			throw new BusinessException("Error creating cda :" ,ex);
		}
	}
	
	@Override
	public String get(final String txID) {
		try {
			return cdaRepo.getItem(txID);
		} catch(Exception ex) {
			log.error("Error getting cda", ex);
			throw new BusinessException("Error getting cda", ex);
		}
	}

	@Override
	public boolean validateHash(final String hashToValidate, final String txID) {
		String hash = null;

		try {
			hash = cdaRepo.getItem(txID);
			return hashToValidate.equals(hash);
		} catch (Exception e) {
			log.error(String.format("Error while retrieving item with transaction ID: %s from Redis.", txID), e);
			throw new BusinessException(String.format("Error while retrieving item with transaction ID: %s from Redis.", txID), e);
		}
	}
	
	@Override
	public boolean consumeHash(final String hashToConsume) {
		try {
			return cdaRepo.delete(hashToConsume);
		} catch (Exception e) {
			log.error("Error while consume hash :" , e);
			throw new BusinessException("Error while consume hash :" , e);
		}
	}

}
