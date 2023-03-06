package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
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
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IEngineSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.PDFUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccreditamentoSimulationSRV implements IAccreditamentoSimulationSRV {

	private static final List<String> listHashes = Arrays.asList("969a45834832ab380bdb9adcb6d02176f8f72156557ad16e36a3af68fbd4e9c8",
			"2371649a30104a2e5b1701a923fdad059fb1df12c215e5f37203d69ca21f71ee",
			"77d3da7921605819c93a8f16c0185ea498c231cacfc3f779073862f160111d69",
			"2d3c4e750b647482dd673e25576b2223c6b2a41440f64b1038462bfc8298f441",
			"11b5c5094109f4742bddc49f076a5185e7b027979d9c6c7b7f680c7e3c007b93",
			"969a45834832ab380bdb9adcb6d02176f8f72156557ad16e36a3af68fbd4e9c8",
			"c35d87607b5497b2254c57ec98e630316925498a10cc981944a710b0113f124a",
			"1311d32b18f1438aa4e32f4520ef311b44f2cbc5d76ff12b1cad23d029d29c01",
			"afd45caa0550fb18d97b7b26bc2104281d9295dcff39f944f93976eb6c6b2270",
			"827f59de39423b38c3b8f4d7cf6e95f6667a707eddeb3e11f31eeb5d1639ec02",
			"c73259f029bdd2af8116eb96b30795a3c6325d4053553c9c526142f9973defec",
			"402c1cb00215daa6ba690a6307f42f08f38e122896373e7ac1f22371c15fc3e5",
			"75ddea9a511f9c9155d170105f7c2cfddd8b2c7d963896d370a223abe861a0fc",
			"9e6e50ffdbdc2d68955ea14240f325089b9f57be2248385704fe23af2582eca2",
			"b72ec903549ada6a680cbd30e5c1b35d43c9f56b61c0bb344ab43779d913ec59",
			"9ae9f78b43e479accd8539d7a3d46494429c927e54346831274b04671eb78e39",
			"131be4399cee5c328fb18e0b18408688bbab8e76a7005137484bd8c40e0d640e");
	
	@Autowired
	private ICdaSRV cdaSRV;

	@Autowired
	private IEngineSRV engines;

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
			case CRASH_WF_EDS:
			case CRASH_INI:
			case CRASH_EDS:
				String pdfHash = StringUtility.encodeHex(pdf);
				if(listHashes.contains(pdfHash)) {
					String wii = simulateSkipValidation(pdf);
					output = new AccreditamentoSimulationDTO(wii);  
					break;
				}

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
		// If we skip validation, we won't have transformID and engineID in our
		// ValidationInfoDTO, so we have to pick them by ourselves
		// emulating the gtw-validator mechanism.
		// getKey() returns engineID, getValue() returns transformID
		Pair<String, String> id = engines.getStructureObjectID(templateIdRoot);
		final String hashedCDA = StringUtility.encodeSHA256B64(cda);
		cdaSRV.create(hashedCDA, workflowInstanceId, id.getValue(), id.getKey());
		return workflowInstanceId;
	}

}
