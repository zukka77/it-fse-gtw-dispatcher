package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.CDACFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.AccreditamentoSimulationDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AccreditamentoPrefixEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ErrorInstanceEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IAccreditamentoSimulationSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.ICdaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.PDFUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccreditamentoSimulationSRV implements IAccreditamentoSimulationSRV {

	@Autowired
	private ICdaSRV cdaSRV;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private CDACFG cdaCFG;
	
	@Override
	public AccreditamentoSimulationDTO runSimulation(final String idDocumento, final byte[] pdf, final EventTypeEnum eventType) {
		AccreditamentoSimulationDTO output = null;
		AccreditamentoPrefixEnum prefixEnum = AccreditamentoPrefixEnum.getStartWith(idDocumento);
		if(prefixEnum!=null) {
			checkIdDocumento(idDocumento, prefixEnum);
			
			switch (prefixEnum) {
			case CRASH_TIMEOUT:
				simulateTimeout();
				break;
			case SKIP_VALIDATION:
				String wii = simulateSkipValidation(pdf);
				output = new AccreditamentoSimulationDTO(wii);  
				break;
			default:
				break;
			}
		}
		return output;
	}

	private void checkIdDocumento(final String idDocumento, AccreditamentoPrefixEnum prefixEnum) {
		boolean val = idDocumento.split(prefixEnum.getPrefix()).length > 1;
		if(!val) {
			final ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(RestExecutionResultEnum.SIMULATION_EXCEPTION.getType())
				.title(RestExecutionResultEnum.SIMULATION_EXCEPTION.getTitle())
				.instance(ErrorInstanceEnum.SIMULATION_EXCEPTION.getInstance())
				.detail("Inserire una string randomica dopo l'id documento").build();
			throw new ValidationException(error);
		}
	}

	private void simulateTimeout() {
		log.info("Timeout simulated");
		throw new ConnectionRefusedException(null, "Timeout simulation");
	}

	private String simulateSkipValidation(byte[] pdf) {
		log.info("Skip validation simulation");
		String cda = PDFUtility.extractCDAFromAttachments(pdf,cdaCFG.getCdaAttachmentName());
		Document docT = Jsoup.parse(cda);
		String templateIdRoot = docT.select("templateid").get(0).attr("root");
		String workflowInstanceId = CdaUtility.getWorkflowInstanceId(docT);
		org.bson.Document doc = test(templateIdRoot);
		final String hashedCDA = StringUtility.encodeSHA256B64(cda);
		cdaSRV.create(hashedCDA, workflowInstanceId, doc.get("_id").toString());
		return workflowInstanceId;
	}
 
	//TODO - Chi la deve fare questa query? il validator o la può fare il dispatcher?
	private org.bson.Document test(String templateIdRoot) {
		Query query = new Query();
		query.addCriteria(Criteria.where("template_id_root").is(templateIdRoot));
		return mongoTemplate.findOne(query, org.bson.Document.class,"transform");
	}
}
