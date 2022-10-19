/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.redis.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.CDACFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.redis.ICdaRepo;

/**
 * 
 * @author CPIERASC
 *
 * CDA Repository.
 */
@Repository
public class CdaRepo extends AbstractRedisRepo implements ICdaRepo {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -3175044102992430693L;
	
	@Autowired
	private transient CDACFG cdaCfg;

	@Override
	public void create(final String hashedCDA, final String wii) {
		set(hashedCDA, wii, cdaCfg.getValidationTTL());
	}

	@Override
	public String getItem(final String hash) {
		return get(hash);
	}
 
	
	@Override
	public Boolean delete(final String hash) {
		return super.delete(hash);
	}
	
}
