package it.finanze.sanita.fse2.ms.gtw.dispatcher.config.audit.filters;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.audit.AuditFilter;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.AuditETY;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class UpdateReqFilter implements AuditFilter {
    @Override
    public boolean match(HttpServletRequest req) {
        return req.getAttribute("UPDATE_REQ") != null;
    }

    @Override
    public AuditETY apply(String uri, HttpServletRequest req, Object body) {
        AuditETY audit = new AuditETY();
        audit.setServizio(uri);
        audit.setStart_time(new Date());
        audit.setEnd_time(new Date());
        audit.setRequest(req.getAttribute("UPDATE_REQ"));
        audit.setResponse(body);
        audit.setHttpMethod(req.getMethod());
        return audit;
    }
}
