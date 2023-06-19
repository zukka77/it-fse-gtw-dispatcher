/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.config.audit;

import javax.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface AuditExclusion {
    boolean verify(String uri, HttpServletRequest req);
}
