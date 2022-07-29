package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 *	@author vincenzoingenito
 *
 */
public interface IAuditSRV extends Serializable {
      
	void saveAuditReqRes(HttpServletRequest httpServletRequest, Object body);
}