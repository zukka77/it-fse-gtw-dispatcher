package it.finanze.sanita.fse2.ms.gtw.dispatcher.config.audit.exclusions;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.audit.AuditExclusion;
import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class SwaggerExclusion implements AuditExclusion {

    @Autowired
    private SwaggerUiConfigProperties swagger;

    @Autowired
    private SpringDocConfigProperties api;

    @Override
    public boolean verify(String uri, HttpServletRequest req) {
        boolean skip = false;
        // Skip check if uri is null or swagger not enabled
        if(uri != null && swagger.isEnabled()) {
            // Swagger page
            String ui = swagger.getPath();
            // Generative API
            String docs = api.getApiDocs().getPath();
            // Retrieve swagger-ui exposed endpoints
            skip = uri.startsWith(ui) || uri.startsWith(docs);
        }
        return skip;
    }
}
