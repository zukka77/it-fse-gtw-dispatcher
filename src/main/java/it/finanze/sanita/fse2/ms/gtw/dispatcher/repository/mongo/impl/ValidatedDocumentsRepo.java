package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.impl;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.MongoException;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.WorkflowIdException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.ValidatedDocumentsETY;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IValidatedDocumentsRepo;
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
			
			Update update = new Update();
			update.set("w_id", ety.getWorkflowInstanceId());
			update.set("insertion_date", new Date());
			mongoTemplate.upsert(query, update, ValidatedDocumentsETY.class);
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
			dto.setTransformId(ety.getPrimaryKeyTransf());
			dto.setStructureId(ety.getPrimaryKeyStructure());
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

		Query query = new Query();
		query.addCriteria(Criteria.where("w_id").is(workflowInstanceId));

		try {
			ValidatedDocumentsETY ety = mongoTemplate.findOne(query, ValidatedDocumentsETY.class);

			Calendar c = Calendar.getInstance();
			
			if (ety != null) {
				c.setTime(ety.getInsertionDate());
				c.add(Calendar.DATE, -days);
			} else
				throw new RuntimeException("Ety is null.");

			Update update = new Update();
			update.set("insertion_date", c.getTime());

			UpdateResult result = mongoTemplate.updateFirst(query, update, ValidatedDocumentsETY.class);

			if (result.getModifiedCount() > 0)
				return ety.getId();
			else
				throw new WorkflowIdException(workflowInstanceId);

		} catch (NullPointerException e) {
			log.error("NullPointerException while update validated document : ", e);
			throw new NullPointerException();
		} catch (Exception ex) {
			log.error("Error while update validated document : ", ex);
			throw new BusinessException("Error while update validated document : ", ex);
		}
	}
}
