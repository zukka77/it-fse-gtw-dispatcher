package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.IDateValidationCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.DateValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDateValidationSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;

@RestController
public class DateValidationCTL extends AbstractCTL implements IDateValidationCTL {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -6908455555111813931L;

	@Autowired
	private IDateValidationSRV dateValidationSRV;

	@Override
	public ResponseEntity<DateValidationDTO> updateValidationDate(final String wiid, final Integer day, HttpServletRequest request) {
		String objectId = dateValidationSRV.updateValidationDate(wiid, day);
		
		if(StringUtility.isNullOrEmpty(objectId)) {
			return new ResponseEntity<>(new DateValidationDTO(getLogTraceInfo(), "Workflow instance id not found"), HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(new DateValidationDTO(getLogTraceInfo(), objectId), HttpStatus.OK);
		}
	}

}
