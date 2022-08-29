package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IIniEdsInvocationRepo;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class IniEdsInvocationRepo implements IIniEdsInvocationRepo {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 7040678303037387997L;

	@Autowired
	private transient MongoTemplate mongoTemplate;
	
	@Override
	public IniEdsInvocationETY insert(final IniEdsInvocationETY ety) {
		IniEdsInvocationETY output = null;
		try {
			output = mongoTemplate.insert(ety);
		} catch(Exception ex) {
			log.error("Error while insert ini invocation item : " , ex);
			throw new BusinessException("Error while insert ini invocation item : " , ex);
		}
		return output;
	}

}
