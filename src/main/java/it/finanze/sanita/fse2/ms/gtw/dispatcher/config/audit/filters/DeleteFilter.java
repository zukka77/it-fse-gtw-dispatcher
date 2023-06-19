/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.config.audit.filters;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.audit.AuditFilter;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.AuditETY;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class DeleteFilter implements AuditFilter {
    @Override
    public boolean match(HttpServletRequest req) {
        return HttpMethod.DELETE.toString().equals(req.getMethod());
    }

    @Override
    public AuditETY apply(String uri, HttpServletRequest req, Object body) {
        AuditETY audit = new AuditETY();
        audit.setServizio(uri);
        audit.setStart_time(new Date());
        audit.setEnd_time(new Date());
        audit.setResponse(body);
        audit.setHttpMethod(req.getMethod());
        return audit;
    }
}
