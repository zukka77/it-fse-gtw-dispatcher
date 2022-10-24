package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import java.io.Serializable;

public interface IDateValidationSRV extends Serializable{

	boolean updateValidationDate(String workflowInstanceId, int days);
}
