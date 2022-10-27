package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.impl;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.MongoException;
import com.mongodb.client.result.DeleteResult;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.ValidatedDocumentsETY;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IValidatedDocumentsRepo;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.DateUtility;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class ValidatedDocumentsRepo implements IValidatedDocumentsRepo {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -3844637790482539072L;

	@Autowired
	private transient MongoTemplate mongoTemplate;

	@Override
	public void create(final ValidatedDocumentsETY ety) {
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("hash_cda").is(ety.getHashCda()));
			
			ValidatedDocumentsETY etyFinded = mongoTemplate.findOne(query, ValidatedDocumentsETY.class);
			if(etyFinded!=null) {
				etyFinded.setPrimaryKeyTransform(ety.getPrimaryKeyTransform());
				etyFinded.setPrimaryKeyXSLT(ety.getPrimaryKeyXSLT());
				etyFinded.setWorkflowInstanceId(ety.getWorkflowInstanceId());
				etyFinded.setInsertionDate(new Date());
				mongoTemplate.save(etyFinded);
			} else {
				ety.setInsertionDate(new Date());
				mongoTemplate.save(ety);
			} 
		} catch (Exception ex) {
			log.error("Error while insert validated document : ", ex);
			throw new BusinessException("Error while insert validated document : ", ex);
		}
	}

	@Override
	public boolean isItemInserted(final String hash) {
		boolean response;
		Query query = new Query();
		query.addCriteria(where("hash_cda").is(hash));
		try {
			response = mongoTemplate.exists(query, ValidatedDocumentsETY.class);
		} catch (MongoException e) {
			log.error("Unable to verify if validate document is inserted", e);
			throw new BusinessException("Unable to verify if validate document is inserted", e);
		}
		return response;
	}

	@Override
	public boolean deleteItem(final String hash) {
		DeleteResult output;
		Query query = new Query();
		query.addCriteria(where("hash_cda").is(hash));
		try {
			output = mongoTemplate.remove(query, ValidatedDocumentsETY.class);
		} catch (MongoException e) {
			log.error("Unable to delete the item", e);
			throw new BusinessException("Unable to delete the item", e);
		}
		return output.wasAcknowledged();
	}

	@Override
	public ValidatedDocumentsETY findItemById(final String id) {
		ValidatedDocumentsETY output;
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(id));
		try {
			output = mongoTemplate.findOne(query, ValidatedDocumentsETY.class);
		} catch (MongoException e) {
			log.error("Unable to retrieve validated doc by id", e);
			throw new BusinessException("Unable to retrieve validated doc by id", e);
		}
		return output;
	}

	@Override
	public ValidationDataDTO findItemByWorkflowInstanceId(final String wid) {
		ValidationDataDTO output;
		ValidatedDocumentsETY ety;
		Query query = new Query();
		query.addCriteria(Criteria.where("w_id").is(wid));
		try {
			ety = mongoTemplate.findOne(query, ValidatedDocumentsETY.class);
			output = parseEtyToDTo(ety);
		} catch (MongoException e) {
			log.error("Unable to retrieve validated doc by hash", e);
			throw new BusinessException("Unable to retrieve validated doc by hash", e);
		}

		return output;
	}

	ValidationDataDTO parseEtyToDTo(ValidatedDocumentsETY ety) {
		// se output null oggetto con validatedFalse
		ValidationDataDTO dto = new ValidationDataDTO();
		if (ety == null) {
			dto.setCdaValidated(false);
		} else {
			dto.setHash(ety.getHashCda());
			dto.setCdaValidated(true);
			dto.setWorkflowInstanceId(ety.getWorkflowInstanceId());
			dto.setXsltID(ety.getPrimaryKeyXSLT());
			dto.setTransformID(ety.getPrimaryKeyTransform());
			dto.setInsertionDate(ety.getInsertionDate());
		}
		return dto;
	}

	@Override
	public ValidationDataDTO findItemByHash(final String hash) {
		ValidationDataDTO output;
		ValidatedDocumentsETY ety;
		Query query = new Query();
		query.addCriteria(Criteria.where("hash_cda").is(hash));
		try {
			ety = mongoTemplate.findOne(query, ValidatedDocumentsETY.class);
			output = parseEtyToDTo(ety);
		} catch (MongoException e) {
			log.error("Unable to retrieve validated doc by hash", e);
			throw new BusinessException("Unable to retrieve validated doc by hash", e);
		}

		return output;
	}

	@Override
	public String updateInsertionDate(final String workflowInstanceId, final int days) {
		String objectId = "";
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("w_id").is(workflowInstanceId));

			ValidatedDocumentsETY ety = mongoTemplate.findOne(query, ValidatedDocumentsETY.class);
			
			if(ety!=null) {
				Date newDate = DateUtility.addDay(ety.getInsertionDate(), -days);
				ety.setInsertionDate(newDate);
				ety = mongoTemplate.save(ety);
				objectId = ety.getId();
			}  
		} catch (Exception ex) {
			log.error("Error while update validated document : ", ex);
			throw new BusinessException("Error while update validated document : ", ex);
		}
		return objectId;
	}
}
