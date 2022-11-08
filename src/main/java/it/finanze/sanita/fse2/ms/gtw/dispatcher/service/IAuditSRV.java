/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

 
public interface IAuditSRV extends Serializable {
      
	void saveAuditReqRes(HttpServletRequest httpServletRequest, Object body);
}