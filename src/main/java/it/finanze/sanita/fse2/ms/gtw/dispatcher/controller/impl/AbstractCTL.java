package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import brave.Tracer;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IValidatorClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.CDACFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.ValidationCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.AttachmentDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTHeaderDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.PublicationOutputDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.HistoricalPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.HistoricalValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.TSPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RawValidationEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.UIDModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.facade.ICdaFacadeSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CfUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.PDFUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author CPIERASC
 *
 *	Abstract controller.
 */
@Slf4j
public abstract class AbstractCTL implements Serializable {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -3077780100650268134L;
	
	@Autowired
	private Tracer tracer;

    @Autowired
	private IValidatorClient validatorClient;

    @Autowired
	private ICdaFacadeSRV cdaFacadeSRV;
	
	@Autowired
	private CDACFG cdaCfg;

	@Value("${sign.verification.mode}")
    private String signVerificationMode;

	@Autowired
	private ValidationCFG validationCFG;

	protected LogTraceInfoDTO getLogTraceInfo() {
		LogTraceInfoDTO out = new LogTraceInfoDTO(null, null);
		if (tracer.currentSpan() != null) {
			out = new LogTraceInfoDTO(
					tracer.currentSpan().context().spanIdString(), 
					tracer.currentSpan().context().traceIdString());
		}
		return out;
	}

	protected ValidationCDAReqDTO getValidationJSONObject(String jsonREQ) {
		ValidationCDAReqDTO out = null;
		if(!StringUtility.isNullOrEmpty(jsonREQ)) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				out = mapper.readValue(jsonREQ, ValidationCDAReqDTO.class);
			} catch (Exception ex) {
				log.error("Errore gestione json :" , ex);
			}
		}
		return out;
	}

	protected HistoricalValidationCDAReqDTO getHistoricalValidationJSONObject(String jsonREQ) {
		HistoricalValidationCDAReqDTO out = null;
		if(!StringUtility.isNullOrEmpty(jsonREQ)) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				out = mapper.readValue(jsonREQ, HistoricalValidationCDAReqDTO.class);
			} catch (Exception ex) {
				log.error("Errore gestione json :" , ex);
			}
		}
		return out;
	}

	protected PublicationCreationReqDTO getPublicationJSONObject(String jsonREQ) {
        PublicationCreationReqDTO out = null;
        if(!StringUtility.isNullOrEmpty(jsonREQ)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                out = mapper.readValue(jsonREQ, PublicationCreationReqDTO.class);
            } catch (Exception ex) {
                log.error("Errore gestione json", ex);
            }
        }

		// If the request does not contain a transaction Id, it can be forced for pablish, a new transaction Id must be generate
		if (out != null && Boolean.TRUE.equals(out.isForcePublish())) {
			out.setWorkflowInstanceId(StringUtility.generateTransactionUID(UIDModeEnum.get(validationCFG.getTransactionIDStrategy())));
		}
        return out;
    }

	protected HistoricalPublicationCreationReqDTO getHistoricalPublicationJSONObject(String jsonREQ) {
        HistoricalPublicationCreationReqDTO out = null;
        if(!StringUtility.isNullOrEmpty(jsonREQ)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                out = mapper.readValue(jsonREQ, HistoricalPublicationCreationReqDTO.class);
            } catch (Exception ex) {
                log.error("Errore gestione json", ex);
            }
        }
        return out;
    }

	protected TSPublicationCreationReqDTO getTSPublicationJSONObject(String jsonREQ) {
        TSPublicationCreationReqDTO out = null;
        if(!StringUtility.isNullOrEmpty(jsonREQ)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                out = mapper.readValue(jsonREQ, TSPublicationCreationReqDTO.class);
            } catch (Exception ex) {
                log.error("Errore gestione json", ex);
            }
        }
        return out;
    }


	protected PublicationCreationReqDTO constructPublicationDTO(HistoricalPublicationCreationReqDTO historicalDTO) {
		return PublicationCreationReqDTO.builder()
		.workflowInstanceId(historicalDTO.getTransactionID())
		.healthDataFormat(historicalDTO.getHealthDataFormat())
        .mode(historicalDTO.getMode())
        .tipologiaStruttura(historicalDTO.getTipologiaStruttura())
        .attiCliniciRegoleAccesso(historicalDTO.getRegoleAccesso())
        .identificativoDoc(historicalDTO.getIdentificativoDoc())
        .identificativoRep(historicalDTO.getIdentificativoRep())
        .tipoDocumentoLivAlto(historicalDTO.getTipoDocumentoLivAlto())
        .assettoOrganizzativo(historicalDTO.getAssettoOrganizzativo())
        .dataInizioPrestazione(historicalDTO.getDataInizioPrestazione())
        .dataFinePrestazione(historicalDTO.getDataFinePrestazione())
        .conservazioneANorma(historicalDTO.getConservazioneANorma())
        .tipoAttivitaClinica(historicalDTO.getTipoAttivitaClinica())
        .identificativoSottomissione(historicalDTO.getIdentificativoSottomissione())
		.forcePublish(historicalDTO.isForcePublish())
        .build();
	}

	protected PublicationCreationReqDTO constructPublicationDTO(TSPublicationCreationReqDTO tsDTO) {
		return PublicationCreationReqDTO.builder()
				.healthDataFormat(tsDTO.getHealthDataFormat())
				.mode(tsDTO.getMode())
				.tipologiaStruttura(tsDTO.getTipologiaStruttura())
				.attiCliniciRegoleAccesso(tsDTO.getRegoleAccesso())
				.identificativoDoc(tsDTO.getIdentificativoDoc())
				.identificativoRep(tsDTO.getIdentificativoRep())
				.tipoDocumentoLivAlto(tsDTO.getTipoDocumentoLivAlto())
				.assettoOrganizzativo(tsDTO.getAssettoOrganizzativo())
				.dataInizioPrestazione(tsDTO.getDataInizioPrestazione())
				.dataFinePrestazione(tsDTO.getDataFinePrestazione())
				.conservazioneANorma(tsDTO.getConservazioneANorma())
				.tipoAttivitaClinica(tsDTO.getTipoAttivitaClinica())
				.identificativoSottomissione(tsDTO.getIdentificativoSottomissione())
				.forcePublish(tsDTO.isForcePublish())
				.build();
	}

    protected String checkValidationMandatoryElements(ValidationCDAReqDTO jsonObj) {
		String out = null;

		if (jsonObj.getActivity() == null) {
			out = "Il campo activity deve essere valorizzato.";
		}  
		return out;
	}

	protected String checkHistoricalValidationMandatoryElements(HistoricalValidationCDAReqDTO jsonObj) {
		String out = null;
		
			if (jsonObj.getActivity()==null) {
				out = "Il campo activity deve essere valorizzato.";
			} else if (jsonObj.getTipoDocumentoLivAlto()==null) {
				out = "Il tipo documento di alto livello deve essere valorizzato.";
			} else if (jsonObj.getAssettoOrganizzativo()==null) {
				out = "L'assetto organizzativo deve essere valorizzato.";
			} else if (!CfUtility.isValidCf(jsonObj.getIdentificativoPaziente())) {
				out = "L'identificativo paziente deve essere valorizzato con un codice fiscale valido.";
			} else if (jsonObj.getTipoAttivitaClinica()==null) {
				out = "Il tipo attività clinica deve essere valorizzata.";
			} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoSottomissione())) {
				out = "L'identificativo della sottomissione deve essere valorizzato.";
			}
		return out;
	}

    protected String checkPublicationMandatoryElements(PublicationCreationReqDTO jsonObj) {
    	String out = null;
    	if (Boolean.FALSE.equals(jsonObj.isForcePublish()) && StringUtility.isNullOrEmpty(jsonObj.getWorkflowInstanceId())) {
    		out = "Il campo txID deve essere valorizzato.";
    	} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoDoc())) {
    		out = "Il campo identificativo documento deve essere valorizzato.";
    	} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoRep())) {
    		out = "Il campo identificativo rep deve essere valorizzato.";
    	} else if (jsonObj.getTipoDocumentoLivAlto()==null) {
    		out = "Il campo tipo documento liv alto deve essere valorizzato.";
    	} else if (jsonObj.getAssettoOrganizzativo()==null) {
    		out = "Il campo assetto organizzativo deve essere valorizzato.";
    	} else if (jsonObj.getTipoAttivitaClinica()==null) {
    		out = "Il campo tipo attivita clinica deve essere valorizzato.";
    	} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoSottomissione())) {
    		out = "Il campo identificativo sottomissione deve essere valorizzato.";
    	} else if(jsonObj.getTipologiaStruttura()==null) {
    		out = "Il campo tipologia struttura deve essere valorizzato.";
    	}
    	return out;
    }

	protected JWTTokenDTO extractJWT(final String jwt) {
		JWTTokenDTO jwtToken = null;

		try {
			if (!StringUtility.isNullOrEmpty(jwt)) {
				
				log.debug("Decoding JWT");
				
				String[] chunks = null;
				String header = null;
				String payload = null;
				if (!jwt.startsWith(Constants.App.BEARER_PREFIX)) {
					log.warn("Bearer prefix not found, trying decoding the token without prefix");
					chunks = jwt.split("\\.");
					payload = new String(Base64.getDecoder().decode(chunks[0]));

					// Building the object asserts that all required values are present
					jwtToken = new JWTTokenDTO(null, JWTPayloadDTO.extractPayload(payload));
				} else {
					// Getting header and payload removing the "Bearer " prefix
					chunks = jwt.substring(Constants.App.BEARER_PREFIX.length()).split("\\.");
					header = new String(Base64.getDecoder().decode(chunks[0]));
					payload = new String(Base64.getDecoder().decode(chunks[1]));

					// Building the object asserts that all required values are present
					jwtToken = new JWTTokenDTO(JWTHeaderDTO.extractHeader(header), JWTPayloadDTO.extractPayload(payload));
				}
			}
		} catch (Exception e) {
			log.error("Error while reading JWT payload", e);
		}

		return jwtToken;
	}

	protected String validateJWT(final JWTTokenDTO jwtToken, final String cda) {

		String errorMsg = null;
		try {

			if (jwtToken == null || StringUtils.isEmpty(cda)) {
				errorMsg = "JWT payload or CDA is null or empty";
			} else {
				org.jsoup.nodes.Document docT = Jsoup.parse(cda);
				String code = docT.select("code").get(0).attr("code");
				String codeSystem = docT.select("code").get(0).attr("codeSystem");
				String hl7Type = code+"^^"+codeSystem;
				String patientRoleCF = docT.select("patientRole > id").get(0).attr("extension");
				
				if(!hl7Type.equals(jwtToken.getPayload().getResource_hl7_type())) {
					errorMsg = "JWT payload : tipo doc diverso dal code and codesystem del cda";
				}
				
				if(StringUtility.isNullOrEmpty(errorMsg)) {
					final String [] chunks = jwtToken.getPayload().getPerson_id().split("\\^");
					if(!chunks[0].equals(patientRoleCF)) {
						errorMsg = "JWT payload : person id diverso dal patient del cda";
					}
				}
			}
		} catch (Exception e) {
			log.error("Error while validating JWT payload with CDA", e);
			errorMsg = "Errore durante la validazione del JWT rispetto al CDA";
		}
		return errorMsg;
	}

	protected String checkTSPublicationMandatoryElements(TSPublicationCreationReqDTO jsonObj) {
    	String out = null;
    	if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoDoc())) {
    		out = "Il campo identificativo documento deve essere valorizzato.";
    	} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoRep())) {
    		out = "Il campo identificativo rep deve essere valorizzato.";
    	} else if (jsonObj.getTipoDocumentoLivAlto()==null) {
    		out = "Il campo tipo documento liv alto deve essere valorizzato.";
    	} else if (jsonObj.getAssettoOrganizzativo()==null) {
    		out = "Il campo assetto organizzativo deve essere valorizzato.";
    	} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoPaziente())) {
    		out = "Il campo identificativo paziente deve essere valorizzato.";
    	} else if (jsonObj.getTipoAttivitaClinica()==null) {
    		out = "Il campo tipo attivita clinica deve essere valorizzato.";
    	} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoSottomissione())) {
    		out = "Il campo identificativo sottomissione deve essere valorizzato.";
    	}
    	return out;
    }

	protected String checkHistoricalPublicationMandatoryElements(HistoricalPublicationCreationReqDTO jsonObj) {
    	String out = null;
    	if (StringUtility.isNullOrEmpty(jsonObj.getTransactionID())) {
    		out = "Il campo txID deve essere valorizzato.";
    	} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoDoc())) {
    		out = "Il campo identificativo documento deve essere valorizzato.";
    	} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoRep())) {
    		out = "Il campo identificativo rep deve essere valorizzato.";
    	} else if (jsonObj.getTipoDocumentoLivAlto()==null) {
    		out = "Il campo tipo documento liv alto deve essere valorizzato.";
    	} else if (jsonObj.getAssettoOrganizzativo()==null) {
    		out = "Il campo assetto organizzativo deve essere valorizzato.";
    	} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoPaziente())) {
    		out = "Il campo identificativo paziente deve essere valorizzato.";
    	} else if (jsonObj.getTipoAttivitaClinica()==null) {
    		out = "Il campo tipo attivita clinica deve essere valorizzato.";
    	} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoSottomissione())) {
    		out = "Il campo identificativo sottomissione deve essere valorizzato.";
    	}
    	return out;
    }
	
	protected String checkTokenMandatory(String jwt) {
    	String out = null;
    	if (StringUtility.isNullOrEmpty(jwt)) {
    		out = "Il JWT deve essere valorizzato.";
    	}
    	return out;
    }
	
	protected byte[] checkFile(MultipartFile file) {
		byte[] out = null;
		try {
			if (file != null && file.getBytes() != null && file.getBytes().length > 0) {
				out = file.getBytes();
			}
		} catch (IOException e1) {
			log.error("Generic error io in cda :", e1);
		}
		return out;
	}
	
	protected String extractCDA(byte[] bytesPDF, InjectionModeEnum mode) {
		String out = null;
		if (InjectionModeEnum.RESOURCE.equals(mode)) {
			out = PDFUtility.unenvelopeA2(bytesPDF);
		} else if (InjectionModeEnum.ATTACHMENT.equals(mode)) {
			out = extractCDAFromAttachments(bytesPDF);
		} else {
			out = PDFUtility.unenvelopeA2(bytesPDF);
			if (StringUtility.isNullOrEmpty(out)) {
				out = extractCDAFromAttachments(bytesPDF);
			}
		}
		return out;
	}

	protected ValidationInfoDTO validate(String cda, ActivityEnum activity, String transactionID) {
		ValidationInfoDTO rawValidationRes = null;
		try {
			rawValidationRes = validatorClient.validate(cda);
			if(rawValidationRes!=null && !ActivityEnum.TS_PRE_PUBLISHING.equals(activity)) {
				RawValidationEnum rawValidation = rawValidationRes.getResult();
				if (RawValidationEnum.OK.equals(rawValidation)) {
					if (ActivityEnum.VALIDATION.equals(activity) || ActivityEnum.HISTORICAL_DOC_PRE_PUBLISHING.equals(activity)) {
						String hashedCDA = StringUtility.encodeSHA256B64(cda);
						cdaFacadeSRV.create(transactionID, hashedCDA);
					}
				}  
			}
		}  catch(ConnectionRefusedException cex) {
			throw cex;
		} catch(Exception ex) {
			log.error("Error while validate : " , ex);
			throw new BusinessException("Error while validate : " , ex);
		}
		return rawValidationRes;
	}

	protected String extractCDAFromAttachments(byte[] cda) {
		String out = null;
		Map<String, AttachmentDTO> attachments = PDFUtility.extractAttachments(cda);
		if (attachments!= null && attachments.size()>0) {
			if (attachments!= null && attachments.size() == 1) {
				out = new String(attachments.values().iterator().next().getContent());
			} else {
				AttachmentDTO attDTO = attachments.get(cdaCfg.getCdaAttachmentName());
				if (attDTO != null) {
					out = new String(attDTO.getContent());
				}
			}
		}
		return out;
	}

    protected PublicationOutputDTO validateDocumentHash(final String encodedPDF, final JWTTokenDTO jwtToken) {

		if (!encodedPDF.equals(jwtToken.getPayload().getAttachment_hash())) {
			return PublicationOutputDTO.builder().msg(PublicationResultEnum.DOCUMENT_HASH_VALIDATION_ERROR.getTitle())
					.result(PublicationResultEnum.DOCUMENT_HASH_VALIDATION_ERROR).build();
		} else {
			return null;
		}
	}

    protected PublicationOutputDTO validateCDAHash(String cda , String transactionID) {
		PublicationOutputDTO out = null; 
        if (!StringUtility.isNullOrEmpty(cda)){
            final String hashedCDA = StringUtility.encodeSHA256B64(cda);
            final boolean matchingHash = cdaFacadeSRV.validateHash(hashedCDA, transactionID);
            if (!matchingHash) {
            	out = PublicationOutputDTO.builder().msg("Il CDA non risulta validato").result(PublicationResultEnum.CDA_MATCH_ERROR).build();
            }
        } else {
        	out = PublicationOutputDTO.builder().msg("Impossibile estrarre il CDA").result(PublicationResultEnum.MINING_CDA_ERROR).build();
        }
        return out;
	}
    
    protected String checkFormatDate(final String dataInizio, final String dataFine) {
    	String out = null;
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    	if (dataInizio!=null) {
    		try {
    			sdf.parse(dataInizio);
    		} catch(Exception ex) {
    			out = "Il campo data inizio deve essere valorizzato correttamente";	
    		}
    	}  
    	
    	if(StringUtility.isNullOrEmpty(out) && dataFine!=null) {
    		try {
    			sdf.parse(dataFine);
    		} catch(Exception ex) {
    			out = "Il campo data fine deve essere valorizzato correttamente";	
    		}
    	}
    	return out;
    }

}
