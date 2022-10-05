package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import org.springframework.stereotype.Repository;

import com.mongodb.MongoException;
import com.mongodb.client.result.DeleteResult;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.ValidatedDocumentsETY;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IValidatedDocumentsRepo;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class ValidatedDocumentsRepo implements IValidatedDocumentsRepo {
    
    @Autowired
    private transient MongoTemplate mongoTemplate;

    @Override
    public ValidatedDocumentsETY create(final ValidatedDocumentsETY ety){
        ValidatedDocumentsETY output = null;
        try{
            output = mongoTemplate.insert(ety);
        } catch(Exception ex){
            log.error("Error while insert validated document : " , ex);
			throw new BusinessException("Error while insert validated document : " , ex);
        }
        return output;
    }

    @Override
    public boolean isItemInserted(String hash) {
        boolean response;
        // Create query
        Query query = new Query();
        query.addCriteria(
            where("hash_cda").is(hash)
        );
        try {
            // Execute
            response = mongoTemplate.exists(query, ValidatedDocumentsETY.class);
        } catch(MongoException e) {
            // Catch data-layer runtime exceptions and turn into a checked exception
            throw new BusinessException("Unable to verify if validate document is inserted" , e);
        }
        // Return data
        return response;
    }

    @Override
    public boolean deleteItem(String hash) {
        DeleteResult output;
        Query query = new Query();
        query.addCriteria(
            where("hash_cda").is(hash)
        );
        try {
            output = mongoTemplate.remove(query, ValidatedDocumentsETY.class);
        } catch(MongoException e){
            throw new BusinessException("Unable to delete the item" , e); 
        }
        return output.wasAcknowledged();
    }

    @Override
    public ValidatedDocumentsETY findItemById(String id) {
        ValidatedDocumentsETY output;
        Query query = new Query();
        query.addCriteria(
            Criteria.where("_id").is(id)
        );
        try {
            output = mongoTemplate.findOne(query, ValidatedDocumentsETY.class);
        } catch(MongoException e){
            throw new BusinessException("Unable to retrieve validated doc by id", e);
        }
        return output;
    }

    ValidationDataDTO parseEtyToDTo (ValidatedDocumentsETY ety){
        // se output null oggetto con validatedFalse
        ValidationDataDTO dto = new ValidationDataDTO();
        if (ety == null){
            dto.setCdaValidated(false);
        } else {
            dto.setHash(ety.getHashCda());
            dto.setCdaValidated(true);
            dto.setWorkflowInstanceId(ety.getWorkflowInstanceId());
        }
        return dto;
    }

    @Override
    public ValidationDataDTO findItemByHash(String hash) {
        ValidationDataDTO output;
        ValidatedDocumentsETY ety;
        Query query = new Query();
        query.addCriteria(
            Criteria.where("hash_cda").is(hash)
        );
        try {
            ety = mongoTemplate.findOne(query, ValidatedDocumentsETY.class);
            output = parseEtyToDTo(ety);
        } catch(MongoException e){
            throw new BusinessException("Unable to retrieve validated doc by hash", e);
        }

        return output;
    }
}
