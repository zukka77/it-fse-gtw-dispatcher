/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.AuditETY;

public interface IAuditRepo {
 
	/**
	 * Salvataggio audit request and response.
	 */
	void save(AuditETY auditETY);

}
