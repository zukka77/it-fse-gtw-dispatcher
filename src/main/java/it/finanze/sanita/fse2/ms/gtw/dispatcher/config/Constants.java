/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

/**
 * Constants application.
 */
public final class Constants {
 
	public static final class Collections {

		/** 
		 * INI EDS Invocation Collection 
		 */
		public static final String INI_EDS_INVOCATION = "ini_eds_invocation";

		/** 
		 * Collection where validated documents info is saved
		 */
		public static final String VALIDATED_DOCUMENTS = "validated_documents"; 
		
		/** 
		 * Collection where validated documents info is saved
		 */
		public static final String AUDIT = "audit";
		

		private Collections() {

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
        public static final String SHA_ERROR = "Errore in fase di calcolo SHA-256";
        public static final String HOST_ERROR = "Error while retrieving host informations";
		public static final String SHA_ALGORITHM = "SHA-256";
		public static final String JWT_MISSING_ISSUER_PLACEHOLDER = "UNDEFINED_JWT_ISSUER";
		public static final String JWT_MISSING_SUBJECT_ROLE = "UNDEFINED_SUBJECT_ROLE";
		public static final String JWT_MISSING_SUBJECT = "UNDEFINED_SUBJECT";
		public static final String JWT_MISSING_LOCALITY = "UNDEFINED_LOCALITY";


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
		 * Pattern of the date in the format required from INI.
		 */
		public static final String INI_DATE_PATTERN = "yyyyMMddHHmmss";
		
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
			public static final String REFERENCE_PATH = "/v1/get-reference/{idDoc}";
			

		}

		public static final class Eds {
		
			private Eds() {}
			public static final String DELETE_PATH = "/v1/documents/{idDoc}";
			public static final String UPDATE_PATH = "/v1/documents/{idDoc}/metadata";
			public static final String ID_DOC_PLACEHOLDER = "{idDoc}";
		}

		public static final class Config {
		
			private Config() {}

			public static final String WHOIS_PATH = "/v1/whois";
			
			public static final String STATUS_PATH = "/status";
		
			public static final String MOCKED_GATEWAY_NAME = "mocked-gateway";
		}
	}
  
	/**
	 *	Constants.
	 */
	private Constants() {

	}

}
