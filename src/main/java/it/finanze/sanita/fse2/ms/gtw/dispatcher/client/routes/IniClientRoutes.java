package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes;

import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.API_VERSION;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.DELETE_PATH;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.IDENTIFIER;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.IDENTIFIER_MS;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.ID_DOC_PATH_PARAM;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.METADATA_PATH;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.REFERENCE_PATH;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.UPDATE_PATH;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;

@Component
public final class IniClientRoutes {

    @Autowired
    private MicroservicesURLCFG microservices;

    public UriComponentsBuilder base() {
        return UriComponentsBuilder.fromHttpUrl(microservices.getIniClientHost());
    }

    public String identifier() {
        return IDENTIFIER;
    }

    public String microservice() {
        return IDENTIFIER_MS;
    }

    public String delete() {
        return base().pathSegment(API_VERSION, DELETE_PATH).build().toUriString();
    }

    public String update() {
        return base().pathSegment(API_VERSION, UPDATE_PATH).build().toUriString();
    }

    public String references(String id) {
        return base().pathSegment(API_VERSION, REFERENCE_PATH, id).build().toUriString();
    }

    public String metadata() {
        return base().pathSegment(API_VERSION, METADATA_PATH).build().toUriString();
    }

}
