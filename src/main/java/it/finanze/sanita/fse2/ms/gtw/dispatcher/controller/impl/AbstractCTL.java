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
package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import brave.Tracer;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IValidatorClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.CDACFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants.App;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants.Headers;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationMetadataReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.*;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IJwtSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.facade.ICdaFacadeSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.PDFUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *	Abstract controller.
 */
@Slf4j
public abstract class AbstractCTL {
	 
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

	protected void validateUpdateMetadataReq(final PublicationMetadataReqDTO out) {
		final String errorMsg = checkUpdateMandatoryElements(out);

		if (errorMsg != null) {
			final ErrorResponseDTO error = ErrorResponseDTO.builder()
					.type(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getType())
					.title(RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR.getTitle())
					.instance(ErrorInstanceEnum.MISSING_MANDATORY_ELEMENT.getInstance())
					.detail(errorMsg).build();
			throw new ValidationException(error);
		}
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
    	if (StringUtility.isNullOrEmpty(jsonObj.getIdentificativoDoc()) && !isReplace) {
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

    	if(out==null && jsonObj.getDescriptions()!=null) {
    		out = validateDescriptions(jsonObj.getDescriptions());
    	} 

    	if(out==null && jsonObj.getAttiCliniciRegoleAccesso()!=null) {
    		for(String attoClinico : jsonObj.getAttiCliniciRegoleAccesso()) {
    			if(EventCodeEnum.fromValue(attoClinico)==null) {
    				out = "Il campo atti clinici " + attoClinico + " non è consentito";
    			}
    		}
    	} 
    	
    	return out;
    }
    
    private String validateDescriptions(final List<String> descriptions) {
    	String out = null;
    	for(String description : descriptions) {
    		String[] splitDescription = description.split("\\^");
    		if(splitDescription.length!=3) {
    			out = "Valorizzare correttamente il campo descriptions rispettando la forma: [CODICE]^[Descrizione]^[OID]";
    			break;
    		}
    		
    		if(!checkDescription(splitDescription[2])) {
    			out = "Valorizzare correttamente il campo descriptions rispettando i valori di riferimento per gli OID";
    		}
    	}
    	return out;
    }

    private boolean checkDescription(final String oid) {
    	boolean output = false;
    	for(DescriptionEnum desc : DescriptionEnum.values()) {
    		String sanitizedEnumVaue = Pattern.quote(desc.getOid());
    		sanitizedEnumVaue = sanitizedEnumVaue.replace("COD_REGIONE", "(.*)");
    		Pattern pattern = Pattern.compile(sanitizedEnumVaue);
    		Matcher matcher = pattern.matcher(oid);
    		if(matcher.matches()) {
    			String region = matcher.groupCount()>0 ? matcher.group(1) : null;
    			if(StringUtility.isNullOrEmpty(region) || SubjectOrganizationEnum.getCode(region)!=null) {
    				output = true;
    				break;
    			}
    		}
    	}
    	return output;
    }
    
	protected String checkUpdateMandatoryElements(final PublicationMetadataReqDTO jsonObj) {
		String out = null;
		
		if (jsonObj.getTipoDocumentoLivAlto()==null) {
    		out = "Il campo tipo documento liv alto deve essere valorizzato.";
    	} 
		
		if (out==null && jsonObj.getAttiCliniciRegoleAccesso() != null) {
			for (String attoClinico : jsonObj.getAttiCliniciRegoleAccesso()) {
				if (EventCodeEnum.fromValue(attoClinico)==null) {
					out = "Il campo atti clinici " + attoClinico + " non è consentito";
				}
			}
		} 
		
		if(out==null) {
			out = checkFormatDate(jsonObj.getDataInizioPrestazione(), jsonObj.getDataFinePrestazione());
			
			if(out==null && jsonObj.getDescriptions()!=null) {
				out = validateDescriptions(jsonObj.getDescriptions());
			}
    	}
		return out;
	}

	protected JWTPayloadDTO extractAndValidateJWT(final HttpServletRequest request ,final EventTypeEnum eventType) {
		String extractedToken = Boolean.TRUE.equals(msCfg.getFromGovway()) ? request.getHeader(Headers.JWT_GOVWAY_HEADER) : request.getHeader(Headers.JWT_HEADER);
		return extractAndValidateJWT(extractedToken,eventType);
	}
	
	protected JWTPayloadDTO extractAndValidateJWT(final String jwt,EventTypeEnum eventType) {

		final JWTTokenDTO token = extractJWT(jwt);
		
		switch (eventType) {
			case PUBLICATION:
				jwtSRV.validatePayloadForCreate(token.getPayload());
				break;
			case UPDATE:
				jwtSRV.validatePayloadForUpdate(token.getPayload());
				break;
			case DELETE:
				jwtSRV.validatePayloadForDelete(token.getPayload());
				break;
			case REPLACE:
				jwtSRV.validatePayloadForReplace(token.getPayload());
				break;
			case VALIDATION:
				jwtSRV.validatePayloadForValidation(token.getPayload());
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + eventType);
		}

		return token.getPayload();
	}

	private JWTTokenDTO extractJWT(final String jwt) {
		JWTTokenDTO jwtToken = null;
		String errorInstance = ErrorInstanceEnum.MISSING_JWT_FIELD.getInstance();
		String detail = RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.getTitle();
		String title = RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.getTitle();
		String type = RestExecutionResultEnum.MANDATORY_ELEMENT_ERROR_TOKEN.getType();
		try {
			if (!StringUtility.isNullOrEmpty(jwt)) {
				log.debug("Decoding JWT");
				
				String[] chunks = null;
				String payload = null;

				if (!jwt.startsWith(App.BEARER_PREFIX)) {
					chunks = jwt.split("\\.");

					if (Boolean.TRUE.equals(msCfg.getFromGovway())) {
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
					chunks = jwt.substring(App.BEARER_PREFIX.length()).split("\\.");
					payload = new String(Base64.getDecoder().decode(chunks[1]));

					// Building the object asserts that all required values are present
					jwtToken = new JWTTokenDTO(JWTPayloadDTO.extractPayload(payload));
				}
			} else {
				type = RestExecutionResultEnum.MISSING_TOKEN.getType();
				title = RestExecutionResultEnum.MISSING_TOKEN.getTitle();
				errorInstance = ErrorInstanceEnum.MISSING_JWT.getInstance();
				detail = "Attenzione il jwt fornito risulta essere vuoto";
				throw new BusinessException("Token missing");
			}
		} catch (final Exception e) {
			log.error("Error while reading JWT payload", e);

			final ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(type)
				.title(title)
				.instance(errorInstance)
				.detail(detail)
				.build();

			throw new ValidationException(error);
		}

		return jwtToken;
	}
	
	protected void validateJWT(final JWTPayloadDTO jwtPayloadToken, final String cda) {
		Document docT = Jsoup.parse(cda);
		validateResourceHl7Type(jwtPayloadToken, docT);
		validatePersonId(jwtPayloadToken, docT);
	}

	private void validateResourceHl7Type(JWTPayloadDTO jwtPayloadToken, Document docT) {
		Elements element = docT.select("code");
		if (element.isEmpty()) {
			String message = "JWT payload: non è stato possibile verificare la tipologia del CDA";
			throwInvalidTokenError(ErrorInstanceEnum.DOCUMENT_TYPE_MISMATCH, message);
		}
		
		String code = element.get(0).attr("code");
		String codeSystem = element.get(0).attr("codeSystem");
		String hl7Type = "('" + code + "^^" + codeSystem + "')";
		if(!hl7Type.equals(jwtPayloadToken.getResource_hl7_type())) {
			String message = "JWT payload: Tipologia documento diversa dalla tipologia di CDA (code - codesystem)";
			throwInvalidTokenError(ErrorInstanceEnum.DOCUMENT_TYPE_MISMATCH, message);
		}
	}
	
	private void validatePersonId(JWTPayloadDTO jwtPayloadToken, Document docT) {
		Elements element = docT.select("patientRole > id");
		if (element.isEmpty()) {
			String message = "JWT payload: non è stato possibile verificare il codice fiscale del paziente presente nel CDA";
			throwInvalidTokenError(ErrorInstanceEnum.PERSON_ID_MISMATCH, message);
		}
		
		String patientRoleCF = element.get(0).attr("extension");
		String[] chunks = jwtPayloadToken.getPerson_id().split("\\^");
		if(!chunks[0].equals(patientRoleCF)) { 
			String message = "JWT payload: Person id presente nel JWT differente dal codice fiscale del paziente previsto sul CDA";
			throwInvalidTokenError(ErrorInstanceEnum.PERSON_ID_MISMATCH, message);
		}
		
	}

	private void throwInvalidTokenError(ErrorInstanceEnum errorInstance, String errorMessage) {
		ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getType())
				.title(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getTitle())
				.instance(errorInstance.getInstance())
				.detail(errorMessage)
				.build();
		throw new ValidationException(error);
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
			out = PDFUtility.extractCDAFromAttachments(bytesPDF, cdaCfg.getCdaAttachmentName());  
		} else {
			out = PDFUtility.unenvelopeA2(bytesPDF);
			if (StringUtility.isNullOrEmpty(out)) {
				out = PDFUtility.extractCDAFromAttachments(bytesPDF, cdaCfg.getCdaAttachmentName());  
			}
		}

		if (StringUtility.isNullOrEmpty(out)) {
			final ErrorResponseDTO error = ErrorResponseDTO.builder()
				.title(RestExecutionResultEnum.MINING_CDA_ERROR.getTitle())
				.type(RestExecutionResultEnum.MINING_CDA_ERROR.getType())
				.instance(ErrorInstanceEnum.CDA_EXTRACTION.getInstance())
				.detail(ErrorInstanceEnum.CDA_EXTRACTION.getDescription()).build();
				
			throw new ValidationException(error);
		}
		return out;
	}

	protected String cdaWithoutLegalAuthenticator(final String cda) {
		Document doc = Jsoup.parse(cda, "", Parser.xmlParser());
		Element authenticator = doc.selectFirst("LegalAuthenticator");
		if(authenticator != null) {
			authenticator.forEach(e -> {
				// Reset attributes
				e.attributes().forEach(a -> a.setValue("PLACEHOLDER"));
				// Reset values on node without children and with text
				if(e.children().isEmpty() && !e.text().isEmpty()) e.text("PLACEHOLDER");
			});
		} else {
			log.warn("Unable to calculate cda-hash correctly because LegalAuthenticator doesn't exists");
		}
		return doc.toString();
	}

	protected String validate(final String cda, final ActivityEnum activity, final String workflowInstanceId, final String issuer) {
		String errorDetail = "";
		try {
			final ValidationInfoDTO rawValRes = validatorClient.validate(cda,workflowInstanceId, jwtSRV.getSystemByIssuer(issuer));

			if (ActivityEnum.VALIDATION.equals(activity)
					&& Arrays.asList(RawValidationEnum.OK, RawValidationEnum.SEMANTIC_WARNING).contains(rawValRes.getResult())) {

				final String hashedCDA = StringUtility.encodeSHA256B64(cdaWithoutLegalAuthenticator(cda));
				cdaFacadeSRV.create(hashedCDA, workflowInstanceId, rawValRes.getTransformID(), rawValRes.getEngineID());
			}

			if (!RawValidationEnum.OK.equals(rawValRes.getResult())) {
				final RestExecutionResultEnum result = RestExecutionResultEnum.fromRawResult(rawValRes.getResult());
				errorDetail = result.getTitle();
				if (!CollectionUtils.isEmpty(rawValRes.getMessage())) {
					errorDetail = String.join(",", rawValRes.getMessage());
				}
				
				
				if(!RawValidationEnum.SEMANTIC_WARNING.equals(rawValRes.getResult())){
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
			throw new BusinessException("Errore in validazione: " + ex.getMessage());
		}
		return errorDetail;
	}


    protected void validateDocumentHash(final String encodedPDF, final JWTPayloadDTO jwtPayloadToken) {

		if (!encodedPDF.equalsIgnoreCase(jwtPayloadToken.getAttachment_hash())) {

			final ErrorResponseDTO error = ErrorResponseDTO.builder()
					.title(RestExecutionResultEnum.DOCUMENT_HASH_VALIDATION_ERROR.getTitle())
					.type(RestExecutionResultEnum.DOCUMENT_HASH_VALIDATION_ERROR.getType())
					.instance(ErrorInstanceEnum.DIFFERENT_HASH.getInstance())
					.detail(RestExecutionResultEnum.DOCUMENT_HASH_VALIDATION_ERROR.getTitle()).build();
			
			throw new ValidationException(error);
		}
	}

	/**
	 * Retrieve validation info of CDA on MongoDB.
	 * 
	 * @param cda CDA to check validation of.
	 * @param wii WorkflowInstanceId, is not mandatory in publication. If not
	 *            provided, the system will retrieve it from validation info.
	 * @throws ValidationException If the hash does not exists or is associated with a different {@code wii}
	 */
    protected ValidationDataDTO getValidationInfo(final String cda, @Nullable final String wii) {
		final String hashedCDA = StringUtility.encodeSHA256B64(cdaWithoutLegalAuthenticator(cda));

		ValidationDataDTO validationInfo = cdaFacadeSRV.retrieveValidationInfo(hashedCDA, wii);
		if (!validationInfo.isCdaValidated()) {
			final ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(RestExecutionResultEnum.CDA_MATCH_ERROR.getType())
				.title(RestExecutionResultEnum.CDA_MATCH_ERROR.getTitle())
				.instance(ErrorInstanceEnum.CDA_NOT_VALIDATED.getInstance())
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
    
    
    protected PublicationCreationReqDTO getAndValidateValdaPublicationReq(final String jsonREQ) {

    	final PublicationCreationReqDTO out = StringUtility.fromJSONJackson(jsonREQ, PublicationCreationReqDTO.class);
    	String errorMsg = checkPublicationMandatoryElements(out, false);

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
  

}
