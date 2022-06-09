package it.finanze.sanita.fse2.ms.gtw.dispatcher.utility;

import org.jsoup.Jsoup;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class CdaUtility {
 
	/**
	 * Private constructor to avoid instantiation.
	 */
	private CdaUtility() {
		// Constructor intentionally empty.
	}

	
	public static String getWorkflowInstanceId(final String cda) {
		String cxi = extractInfo(cda);	
		return cxi + "." + StringUtility.generateTransactionUID(null) + "^^^^urn:ihe:iti:xdw:2013:workflowInstanceId";
	}
	
	private static String extractInfo(final String cda) {
		String out = "";
		try {
			org.jsoup.nodes.Document docT = Jsoup.parse(cda);
			
			String id = docT.select("id").get(0).attr("root");
			String extension = docT.select("id").get(0).attr("extension");
			
			out = id + "." + extension;
		} catch(Exception ex) {
			log.error("Error while extracting info from cda", ex);
			throw new BusinessException("Error while extracting info from cda", ex);
		}
		return out;
	}
}
