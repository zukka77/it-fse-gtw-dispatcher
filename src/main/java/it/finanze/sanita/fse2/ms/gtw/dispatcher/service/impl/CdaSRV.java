package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.redis.ICdaRepo;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.ICdaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
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
	public void create(final String hashedCDA, final String wii) {
		try {
			cdaRepo.create(hashedCDA, wii);
		} catch(Exception ex) {
			log.error("Error creating cda :" ,ex);
			throw new BusinessException("Error creating cda :" ,ex);
		}
	}
	
	@Override
	public String get(final String hash) {
		try {
			return cdaRepo.getItem(hash);
		} catch(Exception ex) {
			log.error("Error getting workflow instance id from Redis", ex);
			throw new BusinessException("Error getting workflow instance id from Redis", ex);
		}
	}

	@Override
	public ValidationDataDTO retrieveValidationInfo(final String hashPublication, final String wiiPublication) {
		ValidationDataDTO data = new ValidationDataDTO();
		data.setCdaValidated(false);
		data.setHash(hashPublication);

		try {
			final String value = cdaRepo.getItem(hashPublication);

			if (value == null) {
				log.debug("Hash of CDA not found in redis, the CDA may be not validated");
			} else {
				data.setCdaValidated(true);
				data.setWorkflowInstanceId(value);
				if (!StringUtility.isNullOrEmpty(wiiPublication) && !wiiPublication.equals(data.getWorkflowInstanceId())) {
					data.setCdaValidated(false);
				}
			}
		} catch (Exception e) {
			log.error(String.format("Error while retrieving item with transaction ID: %s from Redis.", wiiPublication), e);
			throw new BusinessException(String.format("Error while retrieving item with transaction ID: %s from Redis.", wiiPublication), e);
		}

		return data;
	}
	
	@Override
	public boolean consumeHash(final String hashToConsume) {
		boolean consumed = false;
		try {
			if(!StringUtility.isNullOrEmpty(hashToConsume)) {
				consumed = cdaRepo.delete(hashToConsume);
			}
		} catch (Exception e) {
			log.error("Error while consuming hash from Redis", e);
			throw new BusinessException("Error while consuming hash from Redis", e);
		}
		return consumed;
	}

	 

}
