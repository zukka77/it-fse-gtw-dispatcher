/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
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
