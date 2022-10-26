package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.IDateValidationCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDateValidationSRV;

@RestController
public class DateValidationCTL extends AbstractCTL implements IDateValidationCTL {

	@Autowired
	private IDateValidationSRV dateValidationSRV;

	@Override
	public String updateValidationDate(String workflowInstanceId, int days, HttpServletRequest request) {
		
		try {
			return dateValidationSRV.updateValidationDate(workflowInstanceId, days);
		} catch (Exception ex) {
			throw new BusinessException("Error update validated document while srv invocation event : ", ex);
		}
		
	}

}
