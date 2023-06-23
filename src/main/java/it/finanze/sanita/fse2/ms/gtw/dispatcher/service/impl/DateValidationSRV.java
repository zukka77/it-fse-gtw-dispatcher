/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
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
