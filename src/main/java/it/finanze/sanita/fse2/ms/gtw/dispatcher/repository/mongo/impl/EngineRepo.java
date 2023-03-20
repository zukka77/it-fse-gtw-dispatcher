/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.impl;


import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IEngineRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.stereotype.Repository;

import static it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.engine.EngineETY.*;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
@Slf4j
public class EngineRepo implements IEngineRepo {

	@Autowired
	private MongoTemplate mongo;
 
	@Override
	public EngineETY getLatestEngine() {

		EngineETY out;

		TypedAggregation<EngineETY> q = new TypedAggregation<>(
			EngineETY.class,
			sort(DESC, FIELD_LAST_SYNC),
			match(where(FIELD_AVAILABLE).is(true)),
			limit(1)
		);

		try {
			out = mongo.aggregate(q, EngineETY.class).getUniqueMappedResult();
		} catch(Exception ex) {
			throw new BusinessException("Error while perform find structure map by name : " , ex);
		}
		return out;
	}

}