/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IValidatedDocumentsRepo;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDateValidationSRV;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DateValidationSRV implements IDateValidationSRV {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 6274924321852002956L;
	
	@Autowired
	private IValidatedDocumentsRepo validatedDocumentsRepo; 

	@Override
	public String updateValidationDate(final String workflowInstanceId, final int days) {
		String objectId = "";
		try {
			objectId = validatedDocumentsRepo.updateInsertionDate(workflowInstanceId, days);
		} catch(Exception ex) {
			log.error("Error update validated document  : " , ex);
			throw new BusinessException("Error update validated document : " , ex);
		}
		return objectId;
		
	} 

}
