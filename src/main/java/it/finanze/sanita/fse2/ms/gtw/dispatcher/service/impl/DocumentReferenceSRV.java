package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.FhirMappingClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.DocumentEntryDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.SubmissionSetEntryDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.TransformResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.LowLevelDocEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.ValidatedDocumentsETY;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IValidatedDocumentsRepo;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDocumentReferenceSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DocumentReferenceSRV implements IDocumentReferenceSRV {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 6613399511662450678L;
	private static final String PATH_CUSTODIAN_ID = "ClinicalDocument > custodian > assignedCustodian > representedCustodianOrganization > id";
	private static final String ERROR_MSG = "Error while create document reference request ";
	private static final String EXTENSION_ATTRIBUTE = "extension";

	@Autowired
	private FhirMappingClient client;
	
	@Autowired
	private IValidatedDocumentsRepo validatedDocumentsRepo; 
 
	@Value("${ms.calls.transform-engine}")
	private Boolean callsTransformEngine; 
	
	
	@Override
	public ResourceDTO createFhirResources(final String cda, final PublicationCreationReqDTO requestBody, 
			final Integer size, final String hash, final String sourcePatientId, String transformId, String structureId) {
		final ResourceDTO output = new ResourceDTO();
		try {
			final org.jsoup.nodes.Document docCDA = Jsoup.parse(cda);
			final String encodedCDA = Base64.getEncoder().encodeToString(cda.getBytes());
			
			final DocumentReferenceDTO documentReferenceDTO = buildDocumentReferenceDTO(encodedCDA, requestBody, size, hash, sourcePatientId);
						
			final TransformResDTO resDTO = callFhirMapping(documentReferenceDTO, cda, 
						transformId, structureId); 
			
			if (!StringUtility.isNullOrEmpty(resDTO.getErrorMessage())) {
				output.setErrorMessage(resDTO.getErrorMessage());
			} else {
				output.setBundleJson(StringUtility.toJSON(resDTO.getJson()));
				
				try {
					final SubmissionSetEntryDTO submissionSetEntryDTO = createSubmissionSetEntry(docCDA,requestBody.getTipoAttivitaClinica().getCode(),
							requestBody.getIdentificativoSottomissione());
					output.setSubmissionSetEntryJson(StringUtility.toJSON(submissionSetEntryDTO));
				} catch(final Exception ex) {
					output.setErrorMessage(ex.getCause().getCause().getMessage());
				}
				
				if(StringUtility.isNullOrEmpty(resDTO.getErrorMessage())) {
					try {
						final DocumentEntryDTO documentEntryDTO = createDocumentEntry(docCDA, requestBody, size, hash,sourcePatientId);
						output.setDocumentEntryJson(StringUtility.toJSON(documentEntryDTO));
					} catch(final Exception ex) {
						output.setErrorMessage(ex.getCause().getCause().getMessage());
					}
				}
			}
			
		} catch(final ConnectionRefusedException crex) {
			throw crex;
		} catch(final Exception ex) {
			log.error("Error while running create fhir resources : " ,ex);
			throw new BusinessException("Error while running create fhir resources : " ,ex);
		}
		return output;
	}
	
	private DocumentReferenceDTO buildDocumentReferenceDTO(final String encodedCDA, final PublicationCreationReqDTO requestBody,
			final Integer size, final String hash,final String sourcePatientId) {
		final DocumentReferenceDTO documentReferenceDTO = new DocumentReferenceDTO();
		try {
			documentReferenceDTO.setEncodedCDA(encodedCDA);
			documentReferenceDTO.setSize(size);
			documentReferenceDTO.setHash(hash);
			documentReferenceDTO.setFacilityTypeCode(requestBody.getTipologiaStruttura().getCode());
			
			if(requestBody.getAttiCliniciRegoleAccesso()!=null && !requestBody.getAttiCliniciRegoleAccesso().isEmpty()) { 
				documentReferenceDTO.setEventCode(requestBody.getAttiCliniciRegoleAccesso());
			}
			documentReferenceDTO.setPracticeSettingCode(requestBody.getAssettoOrganizzativo().getDescription());
			documentReferenceDTO.setPatientID(sourcePatientId);
			documentReferenceDTO.setTipoDocumentoLivAlto(requestBody.getTipoDocumentoLivAlto().getCode());
			documentReferenceDTO.setRepositoryUniqueID(requestBody.getIdentificativoRep());
			documentReferenceDTO.setServiceStartTime(requestBody.getDataInizioPrestazione());
			documentReferenceDTO.setServiceStopTime(requestBody.getDataInizioPrestazione());
			documentReferenceDTO.setIdentificativoDoc(requestBody.getIdentificativoDoc());
		} catch(final Exception ex) {
			log.error(ERROR_MSG , ex);
			throw new BusinessException(ERROR_MSG , ex);
		}
		return documentReferenceDTO;
	}
	
	private TransformResDTO callFhirMapping(final DocumentReferenceDTO documentReferenceDTO, final String cda, String structureId, String transformId) {
		TransformResDTO out = null;
		try {
			
			final FhirResourceDTO req = new FhirResourceDTO();
			req.setCda(cda);
			req.setDocumentReferenceDTO(documentReferenceDTO); 
			
			if(callsTransformEngine) {
				req.setObjectId(structureId);
			} else {
				req.setObjectId(transformId);
			}
			out = client.callConvertCdaInBundle(req);
		} catch(final ConnectionRefusedException crex) {
			throw crex;
		} catch(final Exception ex) {
			log.error(ERROR_MSG , ex);
			throw new BusinessException(ERROR_MSG , ex);
		}
		return out;
	}

	
	private SubmissionSetEntryDTO createSubmissionSetEntry(final org.jsoup.nodes.Document docCDA, 
			final String contentTypeCode, final String identificativoSottomissione) {
		SubmissionSetEntryDTO sse = null;
		
		sse = extractedInfoForSubmissionSetEntry(docCDA,identificativoSottomissione);
		sse.setSubmissionTime(new Date().toString());
		sse.setContentTypeCode(contentTypeCode);
		final AttivitaClinicaEnum contentTypeCodeName = Arrays.stream(AttivitaClinicaEnum.values()).filter(attivitaClinicaEnum -> attivitaClinicaEnum.getCode().equals(contentTypeCode)).findFirst().orElse(null);
		if (contentTypeCodeName != null) {
			sse.setContentTypeCodeName(contentTypeCodeName.getDescription());
		} else {
			sse.setContentTypeCodeName(null);
		}
		return sse;
	}
	
	private SubmissionSetEntryDTO extractedInfoForSubmissionSetEntry(final org.jsoup.nodes.Document docCDA, final String identificativoSottomissione) {
		final SubmissionSetEntryDTO sse = new SubmissionSetEntryDTO();
		if (docCDA != null) {
			String sourceIdRoot = "";
			final Element custodianPath = docCDA.select(PATH_CUSTODIAN_ID).first();
			if (custodianPath != null) {
				sourceIdRoot = custodianPath.attr("root");
			}
			
			final Element custodianId = docCDA.select(PATH_CUSTODIAN_ID).first();
			String sourceIdExtension = "";
			if (custodianId != null) {
				sourceIdExtension = custodianId.attr(EXTENSION_ATTRIBUTE);
			}
			sse.setSourceId(sourceIdRoot + "." + sourceIdExtension);
			sse.setUniqueID(identificativoSottomissione);
		}
		return sse;
	}

	
	private DocumentEntryDTO extractedInfoForDocumentEntry(
			final org.jsoup.nodes.Document docCDA, final PublicationCreationReqDTO requestBody) {
		final DocumentEntryDTO documentEntryDTO = new DocumentEntryDTO();

		final Element patientIdElement = docCDA.select(PATH_CUSTODIAN_ID).first();
		if (patientIdElement != null) {
			documentEntryDTO.setPatientId(patientIdElement.attr(EXTENSION_ATTRIBUTE));
		}
		
		final Element confidentialityElement = docCDA.select("ClinicalDocument > confidentialityCode").first();
		if (confidentialityElement != null) {
			confidentialityElement.attr("code");
			documentEntryDTO.setConfidentialityCode(confidentialityElement.attr("code"));
		}

		final Element confidentialityCodeDisplayElement = docCDA.select("ClinicalDocument > confidentialityCode").first();
		if (confidentialityCodeDisplayElement != null) {
			documentEntryDTO.setConfidentialityCodeDisplayName(confidentialityCodeDisplayElement.attr("displayName"));
		}

		final Element typeCodeElement = docCDA.select("ClinicalDocument > code").first();
		if (typeCodeElement != null) {
			documentEntryDTO.setTypeCode(typeCodeElement.attr("code"));
		}

		final Element typeCodeEnumElement = docCDA.select("ClinicalDocument > code").first();
		if (typeCodeEnumElement != null) {
			documentEntryDTO.setTypeCodeName(typeCodeEnumElement.attr("displayName"));
		}

		final Element formatCodeElement = docCDA.select("ClinicalDocument > templateId").first();
		if (formatCodeElement != null) {
			final String formatCode = formatCodeElement.attr("root");
			documentEntryDTO.setFormatCode(formatCode);
			final LowLevelDocEnum formatCodeEnum = Arrays.stream(LowLevelDocEnum.values()).filter(lowLevelDocEnum -> lowLevelDocEnum.getCode().equals(formatCode)).findFirst().orElse(null);
			if (formatCodeEnum != null) {
				documentEntryDTO.setFormatCodeName(formatCodeEnum.getDescription());
			} else {
				documentEntryDTO.setFormatCodeName(null);
			}
		}
		
		final Element legalAuthElement = docCDA.select("ClinicalDocument > legalAuthenticator > assignedEntity > id").first();
		if (legalAuthElement != null) {
			documentEntryDTO.setLegalAuthenticator(legalAuthElement.attr(EXTENSION_ATTRIBUTE));
		}

		final Element sourcePatientElement = docCDA.select("ClinicalDocument > recordTarget > patientRole").first();
		if (sourcePatientElement != null) {
			documentEntryDTO.setSourcePatientInfo(sourcePatientElement.text());
		}

		final Element authorElement = docCDA.select("ClinicalDocument > author").first();
		if (authorElement != null) {
			documentEntryDTO.setAuthor(authorElement.attr(EXTENSION_ATTRIBUTE));
		}
		final Element representedOrganization = docCDA.select("ClinicalDocument > documentationOf > serviceEvent > performer > assignedEntity > representedOrganization").first();
		if (representedOrganization != null) {
			documentEntryDTO.setRepresentedOrganizationName(representedOrganization.select(" > name").text());
			final Element representedOrganizationCodeElement = representedOrganization.select(" > asOrganizationPartOf > id").first();
			if (representedOrganizationCodeElement != null) {
				documentEntryDTO.setRepresentedOrganizationCode(representedOrganizationCodeElement.attr(EXTENSION_ATTRIBUTE));
			}
		}

		final Element rootElement = docCDA.select("ClinicalDocument > id").first();
		String root = "";
		if (rootElement != null) {
			root = rootElement.attr("root");
		}
		final String extension = requestBody.getIdentificativoDoc();
		documentEntryDTO.setUniqueId(root + "^" + extension);
		final Element idFullfillment = docCDA.select("ClinicalDocument > inFulfillmentOf > order > id").first();
		if (idFullfillment != null) {
			final String ref = idFullfillment.attr(EXTENSION_ATTRIBUTE);
			documentEntryDTO.setReferenceIdList(Arrays.asList(ref));
		}
		return documentEntryDTO;
	}

	
	private DocumentEntryDTO createDocumentEntry(final org.jsoup.nodes.Document docCDA,
			final PublicationCreationReqDTO requestBody, final Integer size, final String hash, final String sourcePatientId) {

		DocumentEntryDTO de = null;
		try {
			de = extractedInfoForDocumentEntry(docCDA, requestBody);

			de.setMimeType("application/pdf+text/x-cda-r2+xml");
			de.setEntryUUID("Document01");
			de.setCreationTime(new Date().toString());
			de.setSize(size);
			de.setHash(hash);
			de.setStatus("approved");
			de.setLanguageCode("it-IT");
			de.setHealthcareFacilityTypeCode(requestBody.getTipologiaStruttura().getCode());
			de.setHealthcareFacilityTypeCodeName(requestBody.getTipologiaStruttura().getCode());
			if (!CollectionUtils.isEmpty(requestBody.getAttiCliniciRegoleAccesso())) {
				de.setEventCodeList(requestBody.getAttiCliniciRegoleAccesso());
			}
			de.setRepositoryUniqueId(requestBody.getIdentificativoRep());
			de.setClassCode(requestBody.getTipoDocumentoLivAlto().getCode());
			de.setClassCodeName(requestBody.getTipoDocumentoLivAlto().getDescription());
			de.setPracticeSettingCode(requestBody.getAssettoOrganizzativo().name());
			de.setPracticeSettingCodeName(requestBody.getAssettoOrganizzativo().getDescription());
			de.setSourcePatientId(sourcePatientId);

			final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

			if (requestBody.getDataInizioPrestazione() != null) {
				de.setServiceStartTime(sdf.parse(requestBody.getDataInizioPrestazione()).toString());
			}

			if (requestBody.getDataFinePrestazione() != null) {
				de.setServiceStopTime(sdf.parse(requestBody.getDataFinePrestazione()).toString());
			}
		} catch(final Exception ex) {
			log.error("Error while create document entry : " , ex);
			throw new BusinessException("Error while create document entry : " , ex);
		}
		return de;
	}

}
