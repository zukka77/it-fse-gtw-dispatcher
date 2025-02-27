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
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes;

import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.API_VERSION;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.DELETE_PATH;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.IDENTIFIER;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.IDENTIFIER_MS;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.METADATA_PATH;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.REFERENCE_PATH;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Ini.UPDATE_PATH;

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
