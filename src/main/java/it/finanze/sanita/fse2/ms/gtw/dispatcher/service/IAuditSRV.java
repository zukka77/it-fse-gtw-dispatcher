/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import javax.servlet.http.HttpServletRequest;

 
public interface IAuditSRV {
      
	void saveAuditReqRes(HttpServletRequest httpServletRequest, Object body);
}