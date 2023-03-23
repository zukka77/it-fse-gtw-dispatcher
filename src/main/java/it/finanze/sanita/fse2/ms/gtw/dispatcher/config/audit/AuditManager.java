package it.finanze.sanita.fse2.ms.gtw.dispatcher.config.audit;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.AuditETY;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IAuditRepo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Component
public class AuditManager {

    @Autowired
    private List<AuditExclusion> exclusions;

    @Autowired
    private List<AuditFilter> filters;

    @Autowired
    private IAuditRepo repository;

    public void process(HttpServletRequest req, Object body) {
        // Decoded request url
        String uri = uri(req);
        // If the request must be skipped
        boolean excluded = exclusions.stream().anyMatch(rule -> rule.test(uri, req));
        // Match with appropriate filter
        if(!excluded) {
            Optional<AuditFilter> filter = filters.stream().filter(f -> f.match(req)).findFirst();
            // Check filter exists
            if(filter.isPresent()) {
                // Retrieve audit
                AuditETY entity = filter.get().apply(uri, req, body);
                // Save
                repository.save(entity);
            } else {
                log.debug("No filter found matching the request type");
                log.debug("Skipping audit on path: {}", uri);
            }
        } else {
            log.debug("Audit excluded on path: {}", uri);
        }
    }

    @SneakyThrows
    private String uri(HttpServletRequest req) {
        // Retrieve URI
        String uri = req.getRequestURI();
        // Decode URI request (if not null)
        if(uri != null) uri = URLDecoder.decode(uri, UTF_8.name());
        return uri;
    }

}
