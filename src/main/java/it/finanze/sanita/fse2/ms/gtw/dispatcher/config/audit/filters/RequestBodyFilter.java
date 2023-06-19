/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.config.audit.filters;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.audit.AuditFilter;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.AuditETY;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class RequestBodyFilter implements AuditFilter {
    @Override
    public boolean match(HttpServletRequest req) {
        return req.getParameterMap().get("requestBody") != null;
    }

    @Override
    public AuditETY apply(String uri, HttpServletRequest req, Object body) {
        String[] requestBody = req.getParameterMap().get("requestBody");
        AuditETY audit = new AuditETY();
        audit.setServizio(uri);
        audit.setStart_time((Date)req.getAttribute("START_TIME"));
        audit.setEnd_time(new Date());
        audit.setRequest(StringUtility.fromJSON(requestBody[0], Object.class));
        audit.setResponse(body);
        audit.setJwt_issuer((String)req.getAttribute("JWT_ISSUER"));
        audit.setHttpMethod(req.getMethod());
        req.removeAttribute("JWT_ISSUER");
        return audit;
    }
}
