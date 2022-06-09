package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.redis.impl;

import java.util.concurrent.TimeUnit;

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

	protected void set(String key, Object value, Long ttlSeconds) {
		String json = JsonUtility.objectToJson(value);
		set(key, json, ttlSeconds);
	}

	protected void set(String key, String value, Long ttlSeconds) {
		try {
			redisTemplate.opsForValue().set(key, value);
			if (ttlSeconds!=null) {
				redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
			}
		} catch(Exception ex) {
			log.error("Error set abstract redis repo :" + ex);
			throw new BusinessException("Error set abstract redis repo :" + ex);
		}
	}

	protected String get(String key) { 
		return redisTemplate.opsForValue().get(key);
	}
	
	protected Boolean delete(String key) {
		return redisTemplate.delete(key);
	}

}
