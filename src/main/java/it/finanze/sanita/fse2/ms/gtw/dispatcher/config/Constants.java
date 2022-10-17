package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

/**
 * 
 * @author vincenzoingenito
 *
 * Constants application.
 */
public final class Constants {

	/**
	 *	Path scan.
	 */
	public static final class ComponentScan {

		/**
		 * Base path.
		 */
		public static final String BASE = "it.sanita.dispatcher";

		/**
		 * Controller path.
		 */
		public static final String CONTROLLER = "it.sanita.dispatcher.controller";

		/**
		 * Service path.
		 */
		public static final String SERVICE = "it.sanita.dispatcher.service";

		/**
		 * Configuration path.
		 */
		public static final String CONFIG = "it.sanita.dispatcher.config";
		
		/**
		 * Configuration mongo path.
		 */
		public static final String CONFIG_MONGO = "it.sanita.dispatcher.config.mongo";
		
		/**
		 * Configuration mongo repository path.
		 */
		public static final String REPOSITORY_MONGO = "it.sanita.dispatcher.repository";
		 
		public static final class Collections {

			public static final String INI_EDS_INVOCATION = "ini_eds_invocation";

			private Collections() {

			}
		}
		private ComponentScan() {
			//This method is intentionally left blank.
		}

	}
	
	public static final class Headers {
		
		/**
		 * JWT header field.
		 */
		public static final String JWT_HEADER = "FSE-JWT-Signature";

		/**
		 * JWT header field of GovWay.
		 */
		public static final String JWT_GOVWAY_HEADER = "GovWay-ModI-Info";
		

		private Headers() {
			//This method is intentionally left blank.
		}

	}
 
	public static final class Profile {
		public static final String TEST = "test";
		
		public static final String DEV = "dev";
		
		public static final String DOCKER = "docker";

		public static final String TEST_PREFIX = "test_";

		/** 
		 * Constructor.
		 */
		private Profile() {
			//This method is intentionally left blank.
		}

	}

	public static final class App {
		
		public static final String JWT_TOKEN_TYPE = "JWT";

		public static final String BEARER_PREFIX = "Bearer ";
		public static final String SIGNATURE_INFORMATION_ERROR = "Couldn't get signature information - returning false";
        public static final String SHA_ERROR = "Errore in fase di calcolo SHA-256";
        public static final String HOST_ERROR = "Error while retrieving host informations";
		public static final String SHA_ALGORITHM = "SHA-256";
		public static final String JWT_MISSING_ISSUER_PLACEHOLDER = "UNDEFINED_JWT_ISSUER";
		public static final String JWT_MISSING_SUBJECT_ROLE = "UNDEFINED_SUBJECT_ROLE";

		public static final String MISSING_WORKFLOW_PLACEHOLDER = "UNKNOWN_WORKFLOW_ID";
		public static final String MISSING_DOC_TYPE_PLACEHOLDER = "UNKNOWN_DOCUMENT_TYPE";

		private App() {
			//This method is intentionally left blank.
		}
	}
	
	public static final class OIDS {
        public static final String OID_MEF = "2.16.840.1.113883.2.9.4.3.2";
        
        private OIDS() {
            //This method is intentionally left blank.
        }
    }

	public static final class Misc {
		/**
		 * Prefix for priority queues
		 */
		public static final String LOW_PRIORITY = "_LOW";
		public static final String MEDIUM_PRIORITY = "_MEDIUM";
		public static final String HIGH_PRIORITY = "_HIGH";
        public static final String WARN_EXTRACTION_SELECTION = "Attenzione, non è stata selezionata la modalità di estrazione del CDA";
		/**
		 * Constructor.
		 */
		private Misc() {
			//This method is intentionally left blank.
		}

	}

	public static final class Client {
		private Client() {}
		public static final class Ini {
			private Ini() {}

			public static final String DELETE_PATH = "/v1/ini-delete";
			public static final String UPDATE_PATH = "/v1/ini-update";
		}

		public static final class Eds {
			private Eds() {}
			public static final String DELETE_PATH = "/v1/documents/{idDoc}";
			public static final String UPDATE_PATH = "/v1/documents/{idDoc}/metadata";
			public static final String ID_DOC_PLACEHOLDER = "{idDoc}";
		}
	}
  
	/**
	 *	Constants.
	 */
	private Constants() {

	}

}
