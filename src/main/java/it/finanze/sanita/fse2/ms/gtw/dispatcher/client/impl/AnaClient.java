package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl;


// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
// import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
// import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IAnaClient;
// import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.client.AnaReqDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AnaClient implements IAnaClient {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7874491943915675375L;
	
	// @Autowired
    // private transient RestTemplate restTemplate;
	
	// @Autowired
	// private MicroservicesURLCFG msUrlCFG;
		
	@Override
	public Boolean callAnaClient(final String codFiscale) {
		log.warn("ATTENZIONE , Si sta chiamando il client mockato di Ana , assicurarsi che sia voluto");
		// ResponseEntity<Boolean> response = null;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        AnaReqDTO req = new AnaReqDTO();
        req.setCodiceFiscale(codFiscale);
        
//        HttpEntity<?> entity = new HttpEntity<>(req, headers);
//        try {
//			response = restTemplate.exchange(msUrlCFG.getAnaHost() + "/v1/ValidationForAnaInfo", HttpMethod.POST, entity, Boolean.class);
//        } catch(Exception ex) {
//        	log.error("Generic error while call validation ep :" + ex);
//        	throw new BusinessException("Generic error while call validation ep :" + ex);
//        }
//
//		return response.getBody();

        return true;
	}
	
}
