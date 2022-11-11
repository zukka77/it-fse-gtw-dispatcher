package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base;

public final class ClientRoutes {

    public static final class Ini {

        private Ini() {}
        // COMMON
        public static final String IDENTIFIER = "[INI]";
        // PATH PARAMS
        public static final String ID_DOC_PATH_PARAM = "{id}";
        // ENDPOINT
        public static final String API_VERSION = "v1";
        public static final String DELETE_PATH = "ini-delete";
        public static final String UPDATE_PATH = "ini-update";
        public static final String REFERENCE_PATH = "get-reference";
        public static final  String METADATA_PATH = "get-merged-metadati";
    }

}
