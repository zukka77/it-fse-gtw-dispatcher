package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import javax.annotation.Nullable;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import brave.Tracer;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IValidatorClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.CDACFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.AttachmentDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.TSPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ErrorInstanceEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.InjectionModeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RawValidationEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IJwtSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.facade.ICdaFacadeSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl.UtilitySRV;
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

	@Autowired
	protected MicroservicesURLCFG msCfg;
	
	@Autowired
	private IJwtSRV jwtSRV;

	@Autowired
	private UtilitySRV utilitySrv;

	@Value("${sign.verification.mode}")
    private String signVerificationMode;

	protected LogTraceInfoDTO getLogTraceInfo() {
		LogTraceInfoDTO out = new LogTraceInfoDTO(null, null);
		if (tracer.currentSpan() != null) {
			out = new LogTraceInfoDTO(
					tracer.currentSpan().context().spanIdString(), 
					tracer.currentSpan().context().traceIdString());
		}
		return out;
	}

	protected ValidationCDAReqDTO getAndValidateValidationReq(final String jsonREQ) {
		
		final ValidationCDAReqDTO out = StringUtility.fromJSONJackson(jsonREQ, ValidationCDAReqDTO.class);
		final String errorMsg = checkValidationMandatoryElements(out);

		if (errorMsg != null) {

			final ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getType())
				.title(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getTitle())
				.instance(ErrorInstanceEnum.MISSING_MANDATORY_ELEMENT.getInstance())
				.detail(errorMsg).build();

			throw new ValidationException(error);
		}

		return out;
	}

	protected PublicationCreationReqDTO getAndValidatePublicationReq(final String jsonREQ, final boolean isReplace) {
        
		final PublicationCreationReqDTO out = StringUtility.fromJSONJackson(jsonREQ, PublicationCreationReqDTO.class);
		String errorMsg = checkPublicationMandatoryElements(out, isReplace);

		RestExecutionResultEnum errorType = RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR;
		if (errorMsg == null) {
			errorType = RestExecutionResultEnum.FORMAT_ELEMENT_ERROR; // Assuming the format is wrong
			errorMsg = checkFormatDate(out.getDataInizioPrestazione(), out.getDataFinePrestazione());
		}

		if (errorMsg != null) {

			String errorInstance = ErrorInstanceEnum.MISSING_MANDATORY_ELEMENT.getInstance();
			if (RestExecutionResultEnum.FORMAT_ELEMENT_ERROR.equals(errorType)) {
				errorInstance = ErrorInstanceEnum.INVALID_DATE_FORMAT.getInstance();
			}

			final ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(errorType.getType())
				.title(errorType.getTitle())
				.instance(errorInstance)
				.detail(errorMsg).build();

			throw new ValidationException(error);
		}

        return out;
    }

	protected void validateTSPublicationReq(final TSPublicationCreationReqDTO jsonObj) {
        
		String errorMsg = checkTSPublicationMandatoryElements(jsonObj);
		RestExecutionResultEnum errorType = RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR;

		if (errorMsg == null) {
			errorType = RestExecutionResultEnum.FORMAT_ELEMENT_ERROR;
			errorMsg = checkFormatDate(jsonObj.getDataInizioPrestazione(), jsonObj.getDataFinePrestazione());
		}

		if (errorMsg != null) {
			String errorInstance = ErrorInstanceEnum.MISSING_MANDATORY_ELEMENT.getInstance();
			if (RestExecutionResultEnum.FORMAT_ELEMENT_ERROR.equals(errorType)) {
				errorInstance = ErrorInstanceEnum.INVALID_DATE_FORMAT.getInstance();
			}

			final ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(errorType.getType())
				.title(errorType.getTitle())
				.instance(errorInstance)
				.detail(errorMsg).build();

			throw new ValidationException(error);
		}

    }

	protected PublicationCreationReqDTO constructPublicationDTO(final TSPublicationCreationReqDTO tsDTO) {
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

    protected String checkValidationMandatoryElements(final ValidationCDAReqDTO jsonObj) {
		String out = null;

		if (jsonObj.getActivity() == null) {
			out = "Il campo activity deve essere valorizzato.";
		}  
		
		return out;
	}


    protected String checkPublicationMandatoryElements(final PublicationCreationReqDTO jsonObj, final boolean isReplace) {
    	String out = null;

    	if ((isReplace || Boolean.TRUE.equals(jsonObj.isForcePublish()))
				&& !(StringUtility.isNullOrEmpty(jsonObj.getWorkflowInstanceId())
						|| Constants.App.MISSING_WORKFLOW_PLACEHOLDER.equals(jsonObj.getWorkflowInstanceId()))) {

			out = "Il campo workflow instance id non deve essere valorizzato in modalità force publish oppure in update.";
		} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoDoc()) && !isReplace) {
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
    	} else if(jsonObj.getAttiCliniciRegoleAccesso()!=null) {
    		for(String attoClinico : jsonObj.getAttiCliniciRegoleAccesso()) {
    			if(EventCodeEnum.fromValue(attoClinico)==null) {
    				out = "Il campo event code " + attoClinico + " non è consentito";
    			}
    		}
    	}
    	return out;
    }

	protected JWTTokenDTO extractAndValidateJWT(final String jwt, final boolean isFromGovway) {

		final JWTTokenDTO token = extractJWT(jwt, isFromGovway);

		try {
			String errorMsg = jwtSRV.validatePayload(token.getPayload());
			if (errorMsg != null) {
				final ErrorResponseDTO error = ErrorResponseDTO.builder()
					.type(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getType())
					.title(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getTitle())
					.instance(ErrorInstanceEnum.JWT_MALFORMED_FIELD.getInstance())
					.detail(errorMsg).build();

				throw new ValidationException(error);
			}
		} catch (final ValidationException e) {
			throw e;
		} catch (final Exception ex) {
			log.error("Errore durante la validazione del token JWT", ex);
			throw new BusinessException("Errore durante la validazione del token JWT", ex);
		}

		return token;
	}

	private JWTTokenDTO extractJWT(final String jwt, final boolean isFromGovway) {
		JWTTokenDTO jwtToken = null;
		String errorInstance = ErrorInstanceEnum.MISSING_JWT_FIELD.getInstance();
		try {
			if (!StringUtility.isNullOrEmpty(jwt)) {
				log.debug("Decoding JWT");
				
				String[] chunks = null;
				String payload = null;

				if (!jwt.startsWith(Constants.App.BEARER_PREFIX)) {
					chunks = jwt.split("\\.");

					if (isFromGovway) {
						payload = new String(Base64.getDecoder().decode(chunks[0]));
						// Building the object asserts that all required values are present
						jwtToken = new JWTTokenDTO(JWTPayloadDTO.extractPayload(payload));
					} else {
						payload = new String(Base64.getDecoder().decode(chunks[1]));
						// Building the object asserts that all required values are present
						jwtToken = new JWTTokenDTO(JWTPayloadDTO.extractPayload(payload));
					}
				} else {
					// Getting header and payload removing the "Bearer " prefix
					chunks = jwt.substring(Constants.App.BEARER_PREFIX.length()).split("\\.");
					payload = new String(Base64.getDecoder().decode(chunks[1]));

					// Building the object asserts that all required values are present
					jwtToken = new JWTTokenDTO(JWTPayloadDTO.extractPayload(payload));
				}
			} else {
				errorInstance = ErrorInstanceEnum.MISSING_JWT.getInstance();
				throw new BusinessException("Token missing");
			}
		} catch (final Exception e) {
			log.error("Error while reading JWT payload", e);

			final ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.getType())
				.title(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.getTitle())
				.instance(errorInstance)
				.detail(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.getTitle())
				.build();

			throw new ValidationException(error);
		}

		return jwtToken;
	}

	protected void validateJWT(final JWTTokenDTO jwtToken, final String cda) {
		String errorMessage = "";
		boolean isValidDocType = false;
		try {
			org.jsoup.nodes.Document docT = Jsoup.parse(cda);
			String code = docT.select("code").get(0).attr("code");
			String codeSystem = docT.select("code").get(0).attr("codeSystem");
			String hl7Type = "('" + code + "^^" + codeSystem + "')";
			
			String patientRoleCF = docT.select("patientRole > id").get(0).attr("extension");
			
			if(!hl7Type.equals(jwtToken.getPayload().getResource_hl7_type())) {
				errorMessage = "JWT payload: Tipologia documento diversa dalla tipologia di CDA (code - codesystem)";
				throw new BusinessException(errorMessage);
			}
			
			isValidDocType = true;
			if(StringUtility.isNullOrEmpty(errorMessage)) {
				final String [] chunks = jwtToken.getPayload().getPerson_id().split("\\^");
				if(!chunks[0].equals(patientRoleCF)) {
					errorMessage = "JWT payload: Person id presente nel JWT differente dal codice fiscale del paziente previsto sul CDA";
					throw new BusinessException(errorMessage);
				}
			}
			
		} catch (final Exception e) {
			log.error("Error while validating JWT payload with CDA", e);
			String errorInstance = ErrorInstanceEnum.DOCUMENT_TYPE_MISMATCH.getInstance();
			if (isValidDocType) {
				errorInstance = ErrorInstanceEnum.PERSON_ID_MISMATCH.getInstance();
			}

			final ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getType())
				.title(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getTitle())
				.instance(errorInstance)
				.detail(errorMessage)
				.build();
			throw new ValidationException(error);
		}
	}

	protected String checkTSPublicationMandatoryElements(final TSPublicationCreationReqDTO jsonObj) {
    	String out = null;
    	if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoDoc())) {
    		out = "Il campo identificativo documento deve essere valorizzato.";
    	} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoRep())) {
    		out = "Il campo identificativo rep deve essere valorizzato.";
    	} else if (jsonObj.getTipoDocumentoLivAlto()==null) {
    		out = "Il campo tipo documento liv alto deve essere valorizzato.";
    	} else if (jsonObj.getAssettoOrganizzativo()==null) {
    		out = "Il campo assetto organizzativo deve essere valorizzato.";
    	} else if (!utilitySrv.isValidCf(jsonObj.getIdentificativoPaziente())) {
    		out = "Il campo identificativo paziente deve essere valorizzato.";
    	} else if (jsonObj.getTipoAttivitaClinica()==null) {
    		out = "Il campo tipo attivita clinica deve essere valorizzato.";
    	} else if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoSottomissione())) {
    		out = "Il campo identificativo sottomissione deve essere valorizzato.";
    	} 
    	return out;
    }
	
	protected byte[] getAndValidateFile(final MultipartFile file) {
		byte[] out = null;
		
		try {
			RestExecutionResultEnum result = RestExecutionResultEnum.EMPTY_FILE_ERROR;
			if (file != null && file.getBytes().length > 0) {
				out = file.getBytes();

				result = RestExecutionResultEnum.DOCUMENT_TYPE_ERROR;
				if (PDFUtility.isPdf(out)) {
					result = null;
				}
			}

			if (result != null) {
				String errorInstance = ErrorInstanceEnum.NON_PDF_FILE.getInstance();
				if (RestExecutionResultEnum.EMPTY_FILE_ERROR.equals(result)) {
					errorInstance = ErrorInstanceEnum.EMPTY_FILE.getInstance();
				}
				final ErrorResponseDTO error = ErrorResponseDTO.builder()
					.type(result.getType()).title(result.getTitle())
					.instance(errorInstance).detail(result.getTitle()).build();
				throw new ValidationException(error);
			}
		} catch (final ValidationException validationE) {
			throw validationE;
		} catch (final Exception e) {
			log.error("Generic error io in cda :", e);
			throw new BusinessException(e);
		}
		return out;
	}
	
	protected String extractCDA(final byte[] bytesPDF, final InjectionModeEnum mode) {
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

		if (StringUtility.isNullOrEmpty(out)) {
			final ErrorResponseDTO error = ErrorResponseDTO.builder()
				.title(RestExecutionResultEnum.MINING_CDA_ERROR.getTitle())
				.type(RestExecutionResultEnum.MINING_CDA_ERROR.getType())
				.instance(ErrorInstanceEnum.CDA_EXTRACTION.getInstance())
				.detail(RestExecutionResultEnum.MINING_CDA_ERROR.getTitle()).build();
				
			throw new ValidationException(error);
		}
		return out;
	}

	protected String validate(final String cda, final ActivityEnum activity, final String workflowInstanceId) {
		String errorDetail = "";
		try {
			final ValidationInfoDTO rawValidationRes = validatorClient.validate(cda);

			if (ActivityEnum.VALIDATION.equals(activity)
					&& Arrays.asList(RawValidationEnum.OK, RawValidationEnum.SEMANTIC_WARNING).contains(rawValidationRes.getResult())) {
				final String hashedCDA = StringUtility.encodeSHA256B64(cda);
				cdaFacadeSRV.create(hashedCDA, workflowInstanceId);
			}

			if (!RawValidationEnum.OK.equals(rawValidationRes.getResult())) {
				final RestExecutionResultEnum result = RestExecutionResultEnum.fromRawResult(rawValidationRes.getResult());
				errorDetail = result.getTitle();
				if (!CollectionUtils.isEmpty(rawValidationRes.getMessage())) {
					errorDetail = String.join(",", rawValidationRes.getMessage());
				}
				
				
				if(!RawValidationEnum.SEMANTIC_WARNING.equals(rawValidationRes.getResult())){
					final ErrorResponseDTO error = ErrorResponseDTO.builder()
							.type(result.getType()).title(result.getTitle())
							.instance("/validation/error").detail(errorDetail).build();
	
					throw new ValidationException(error);
				}
			}
		} catch (final ValidationException | ConnectionRefusedException valE) {
			throw valE;
		} catch (final Exception ex) {
			log.error("Error while validate: ", ex);
			throw new BusinessException("Error while validate: ", ex);
		}
		return errorDetail;
	}

	protected String extractCDAFromAttachments(final byte[] cda) {
		String out = null;
		final Map<String, AttachmentDTO> attachments = PDFUtility.extractAttachments(cda);
		if (!attachments.isEmpty()) {
			if (attachments.size() == 1) {
				out = new String(attachments.values().iterator().next().getContent());
			} else {
				final AttachmentDTO attDTO = attachments.get(cdaCfg.getCdaAttachmentName());
				if (attDTO != null) {
					out = new String(attDTO.getContent());
				}
			}
		}
		return out;
	}

    protected void validateDocumentHash(final String encodedPDF, final JWTTokenDTO jwtToken) {

		if (!encodedPDF.equals(jwtToken.getPayload().getAttachment_hash())) {

			final ErrorResponseDTO error = ErrorResponseDTO.builder()
					.title(RestExecutionResultEnum.DOCUMENT_HASH_VALIDATION_ERROR.getTitle())
					.type(RestExecutionResultEnum.DOCUMENT_HASH_VALIDATION_ERROR.getType())
					.instance(ErrorInstanceEnum.DIFFERENT_HASH.getInstance())
					.detail(RestExecutionResultEnum.DOCUMENT_HASH_VALIDATION_ERROR.getTitle()).build();
			
			throw new ValidationException(error);
		}
	}

	/**
	 * Retrieve validation info of CDA on Redis.
	 * 
	 * @param cda CDA to check validation of.
	 * @param wii WorkflowTransactionId, is not mandatory in publication. If not
	 *            provided, the system will retrieve it from validation info.
	 * @throws ValidationException If the hash does not exists or is associated with a different {@code wii}
	 */
    protected ValidationDataDTO getValidationInfo(final String cda, @Nullable final String wii) {
		final String hashedCDA = StringUtility.encodeSHA256B64(cda);

		ValidationDataDTO validationInfo = cdaFacadeSRV.retrieveValidationInfo(hashedCDA, wii);
		if (!validationInfo.isCdaValidated()) {
			final ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(RestExecutionResultEnum.CDA_MATCH_ERROR.getType())
				.title(RestExecutionResultEnum.CDA_MATCH_ERROR.getTitle())
				.instance(RestExecutionResultEnum.CDA_MATCH_ERROR.getType())
				.detail("Il CDA non risulta validato").build();
			
			throw new ValidationException(error);
		} else {
			return validationInfo;
		}
	}

    protected String checkFormatDate(final String dataInizio, final String dataFine) {
    	String out = null;
    	final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    	if (dataInizio!=null) {
    		try {
    			sdf.parse(dataInizio);
    		} catch(final Exception ex) {
    			out = "Il campo data inizio deve essere valorizzato correttamente";	
    		}
    	}  
    	
    	if(StringUtility.isNullOrEmpty(out) && dataFine!=null) {
    		try {
    			sdf.parse(dataFine);
    		} catch(final Exception ex) {
    			out = "Il campo data fine deve essere valorizzato correttamente";	
    		}
    	}
    	return out;
    }

}
