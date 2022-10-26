package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IValidatedDocumentsRepo;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDateValidationSRV;

@Service
public class DateValidationSRV implements IDateValidationSRV {

	@Autowired
	private IValidatedDocumentsRepo validatedDocumentsRepo; 

	@Override
	public String updateValidationDate(final String workflowInstanceId, final int days) {
		
		try {
			return validatedDocumentsRepo.updateInsertionDate(workflowInstanceId, days);
		
		} catch(Exception ex) {
			throw new BusinessException("Error update validated document while repo invocation event : " , ex);
		}
		
	} 

}
