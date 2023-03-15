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