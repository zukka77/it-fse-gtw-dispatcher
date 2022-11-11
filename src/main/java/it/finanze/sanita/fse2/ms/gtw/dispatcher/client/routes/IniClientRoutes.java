package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.*;

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
        return base().pathSegment(API_VERSION, REFERENCE_PATH, ID_DOC_PATH_PARAM).build(id).toString();
    }

    public String metadata() {
        return base().pathSegment(API_VERSION, METADATA_PATH).build().toUriString();
    }

}
