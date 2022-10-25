package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.IDateValidationCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.WorkflowIdException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDateValidationSRV;

@RestController
public class DateValidationCTL extends AbstractCTL implements IDateValidationCTL {

	@Autowired
	IDateValidationSRV dateValidationSRV;

	@Override
	public boolean updateValidationDate(String workflowInstanceId, int days, HttpServletRequest request) {

		boolean result = dateValidationSRV.updateValidationDate(workflowInstanceId, days);
		
		if(!result)
			throw new WorkflowIdException("w_id: " + workflowInstanceId);
		else
			return result;
	}

}
