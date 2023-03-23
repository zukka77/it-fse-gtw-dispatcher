package it.finanze.sanita.fse2.ms.gtw.dispatcher.config.audit;

import javax.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface AuditExclusion {
    boolean test(String uri, HttpServletRequest req);
}
