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
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base;

public final class ClientRoutes {
	
	private ClientRoutes() {}

    public static final class Ini {

        private Ini() {}
        // COMMON
        public static final String IDENTIFIER_MS = "ini";
        public static final String IDENTIFIER = "[INI]";
        // PATH PARAMS
        public static final String ID_DOC_PATH_PARAM = "{id}";
        // ENDPOINT
        public static final String API_VERSION = "v1";
        public static final String DELETE_PATH = "ini-delete";
        public static final String UPDATE_PATH = "ini-update";
        public static final String REFERENCE_PATH = "get-reference";
        public static final String REFERENCE_AUTHOR_PATH = "get-reference-author";
        public static final  String METADATA_PATH = "get-merged-metadati";
    }

}
