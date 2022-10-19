/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.redis.impl;

import java.util.concurrent.TimeUnit;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.ProfileUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.JsonUtility;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public abstract class AbstractRedisRepo {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private ProfileUtility profileUtility;

	protected void set(String key, Object value, Long ttlSeconds) {
		String json = JsonUtility.objectToJson(value);
		String redisKey = checkAndChangeKey(key);
		set(redisKey, json, ttlSeconds);
	}

	protected void set(String key, String value, Long ttlSeconds) {
		try {
			String redisKey = checkAndChangeKey(key);
			redisTemplate.opsForValue().set(redisKey, value);
			if (ttlSeconds!=null) {
				redisTemplate.expire(redisKey, ttlSeconds, TimeUnit.SECONDS);
			}
		} catch(Exception ex) {
			log.error("Error set abstract redis repo :" + ex);
			throw new BusinessException("Error set abstract redis repo :" + ex);
		}
	}

	protected String get(String key) {
		String redisKey = checkAndChangeKey(key);
		return redisTemplate.opsForValue().get(redisKey);
	}
	
	protected Boolean delete(String key) {
		String redisKey = checkAndChangeKey(key);
		return redisTemplate.delete(redisKey);
	}

	protected String checkAndChangeKey(String key) {
		if (profileUtility.isTestProfile() && key != null && !key.isEmpty()) {
			key = Constants.Profile.TEST_PREFIX + key;
		}
		return key;
	}

}
