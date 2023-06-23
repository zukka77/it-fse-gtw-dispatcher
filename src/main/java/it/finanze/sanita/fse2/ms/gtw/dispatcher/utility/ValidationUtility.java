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
package it.finanze.sanita.fse2.ms.gtw.dispatcher.utility;

import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum.OLDER_DAY;

import java.util.Date;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ErrorInstanceEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;


public final class ValidationUtility {

	public static void checkDayAfterValidation(Date insertionDate, Integer dayAllowToPublishAfterValidation) {
		if(DateUtility.getDifferenceDays(insertionDate, new Date()) > dayAllowToPublishAfterValidation) {
			final ErrorResponseDTO error = ErrorResponseDTO.builder()
					.type(OLDER_DAY.getType())
					.title(OLDER_DAY.getTitle())
					.instance(ErrorInstanceEnum.OLDER_DAY.getInstance())
					.detail("Error: cannot publish documents older than " + dayAllowToPublishAfterValidation + " days").build();
			throw new ValidationException(error); 
		}
	}
}