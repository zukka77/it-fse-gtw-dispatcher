package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.bson.internal.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.FhirMappingClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.DocumentEntryDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.SubmissionSetEntryDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.DocumentReferenceResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.LowLevelDocEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
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

	@Autowired
	private FhirMappingClient client;
 
	@Override
	public ResourceDTO createFhirResources(final String cda, final PublicationCreationReqDTO requestBody, 
			final Integer size, final String hash, final String sourcePatientId) {
		ResourceDTO output = new ResourceDTO();
		try {
			org.jsoup.nodes.Document docCDA = Jsoup.parse(cda);
			String encodedCDA = Base64.encode(cda.getBytes());
			
			DocumentReferenceDTO documentReferenceDTO = buildDocumentReferenceDTO(encodedCDA, requestBody, size, hash, sourcePatientId);
			
			DocumentReferenceResDTO resDTO = callFhirMapping(documentReferenceDTO, cda);
			
			if (!StringUtility.isNullOrEmpty(resDTO.getErrorMessage())) {
				output.setErrorMessage(resDTO.getErrorMessage());
			} else {
				output.setBundleJson(resDTO.getJson());
				
				try {
					SubmissionSetEntryDTO submissionSetEntryDTO = createSubmissionSetEntry(docCDA,requestBody.getTipoAttivitaClinica().getCode(),
							requestBody.getIdentificativoSottomissione());
					output.setSubmissionSetEntryJson(StringUtility.toJSON(submissionSetEntryDTO));
				} catch(Exception ex) {
					output.setErrorMessage(ex.getCause().getCause().getMessage());
				}
				
				if(StringUtility.isNullOrEmpty(resDTO.getErrorMessage())) {
					try {
						DocumentEntryDTO documentEntryDTO = createDocumentEntry(docCDA, requestBody, size, hash,sourcePatientId);
						output.setDocumentEntryJson(StringUtility.toJSON(documentEntryDTO));
					} catch(Exception ex) {
						output.setErrorMessage(ex.getCause().getCause().getMessage());
					}
				}
			}
			
		} catch(ConnectionRefusedException crex) {
			throw crex;
		} catch(Exception ex) {
			log.error("Error while running create fhir resources : " ,ex);
			throw new BusinessException("Error while running create fhir resources : " ,ex);
		}
		return output;
	}
	
	private DocumentReferenceDTO buildDocumentReferenceDTO(final String encodedCDA, final PublicationCreationReqDTO requestBody,
			final Integer size, final String hash,final String sourcePatientId) {
		DocumentReferenceDTO documentReferenceDTO = new DocumentReferenceDTO();
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
		} catch(Exception ex) {
			log.error("Error while create document reference request " , ex);
			throw new BusinessException("Error while create document reference request " , ex);
		}
		return documentReferenceDTO;
	}
	
	private DocumentReferenceResDTO callFhirMapping(final DocumentReferenceDTO documentReferenceDTO, final String cda) {
		DocumentReferenceResDTO out = null;
		try {
			
			FhirResourceDTO req = new FhirResourceDTO();
			req.setCda(cda);
			req.setDocumentReferenceDTO(documentReferenceDTO);
			out = client.callConvertCdaInBundle(req);
		} catch(ConnectionRefusedException crex) {
			throw crex;
		} catch(Exception ex) {
			log.error("Error while create document reference request " , ex);
			throw new BusinessException("Error while create document reference request " , ex);
		}
		return out;
	}

	
	private SubmissionSetEntryDTO createSubmissionSetEntry(final org.jsoup.nodes.Document docCDA, 
			final String contentTypeCode, String identificativoSottomissione) {
		SubmissionSetEntryDTO sse = null;
		
		sse = extractedInfoForSubmissionSetEntry(docCDA,identificativoSottomissione);
		sse.setSubmissionTime(new Date().toString());
		sse.setContentTypeCode(contentTypeCode);
		AttivitaClinicaEnum contentTypeCodeName = Arrays.stream(AttivitaClinicaEnum.values()).filter(attivitaClinicaEnum -> attivitaClinicaEnum.getCode().equals(contentTypeCode)).findFirst().orElse(null);
		if (contentTypeCodeName != null) {
			sse.setContentTypeCodeName(contentTypeCodeName.getDescription());
		} else {
			sse.setContentTypeCodeName(null);
		}
		return sse;
	}
	
	private SubmissionSetEntryDTO extractedInfoForSubmissionSetEntry(final org.jsoup.nodes.Document docCDA, String identificativoSottomissione) {
		SubmissionSetEntryDTO sse = new SubmissionSetEntryDTO();
		String sourceIdRoot = docCDA.select("ClinicalDocument > custodian > assignedCustodian > representedCustodianOrganization > id").first().attr("root");
		String sourceIdExtension = docCDA.select("ClinicalDocument > custodian > assignedCustodian > representedCustodianOrganization > id").first().attr("extension");
		sse.setSourceId(sourceIdRoot + "." + sourceIdExtension);
		sse.setUniqueID(identificativoSottomissione);
		return sse;
	}

	
	private DocumentEntryDTO extractedInfoForDocumentEntry(
			org.jsoup.nodes.Document docCDA, PublicationCreationReqDTO requestBody) {
		DocumentEntryDTO documentEntryDTO = new DocumentEntryDTO();
		String patientID = docCDA.select("ClinicalDocument > custodian > assignedCustodian > representedCustodianOrganization > id").first().attr("extension");
		documentEntryDTO.setPatientId(patientID);
		String confidentialityCode = docCDA.select("ClinicalDocument > confidentialityCode").first().attr("code");
		String confidentialityCodeDisplayName = docCDA.select("ClinicalDocument > confidentialityCode").first().attr("displayName");
		documentEntryDTO.setConfidentialityCode(confidentialityCode);
		documentEntryDTO.setConfidentialityCodeDisplayName(confidentialityCodeDisplayName);
		String typeCode = docCDA.select("ClinicalDocument > code").first().attr("code");
		documentEntryDTO.setTypeCode(typeCode);
		String typeCodeName = docCDA.select("ClinicalDocument > code").first().attr("displayName");
		documentEntryDTO.setTypeCodeName(typeCodeName);
		String formatCode = docCDA.select("ClinicalDocument > templateId").first().attr("root");
		LowLevelDocEnum formatCodeEnum = Arrays.stream(LowLevelDocEnum.values()).filter(lowLevelDocEnum -> lowLevelDocEnum.getCode().equals(formatCode)).findFirst().orElse(null);
		documentEntryDTO.setFormatCode(formatCode);
		if (formatCodeEnum != null) {
			documentEntryDTO.setFormatCodeName(formatCodeEnum.getDescription());
		} else {
			documentEntryDTO.setFormatCodeName(null);
		}
		String legalAuth = docCDA.select("ClinicalDocument > legalAuthenticator > assignedEntity > id").first().attr("extension");
		documentEntryDTO.setLegalAuthenticator(legalAuth);
		String sourcePatientInfo = docCDA.select("ClinicalDocument > recordTarget > patientRole").first().text();
		documentEntryDTO.setSourcePatientInfo(sourcePatientInfo);
		String authorPerson = docCDA.select("ClinicalDocument > author").first().attr("extension");
		documentEntryDTO.setAuthor(authorPerson);
		Element representedOrganization = docCDA.select("ClinicalDocument > documentationOf > serviceEvent > performer > assignedEntity > representedOrganization").first();
		if (representedOrganization != null) {
			String representedOrganizationName = representedOrganization.select(" > name").text();
			String representedOrganizationCode = representedOrganization.select(" > asOrganizationPartOf > id").first().attr("extension");
			documentEntryDTO.setRepresentedOrganizationName(representedOrganizationName);
			documentEntryDTO.setRepresentedOrganizationCode(representedOrganizationCode);
		}

		String root = docCDA.select("ClinicalDocument > id").first().attr("root");
		String extension = requestBody.getIdentificativoDoc();
		documentEntryDTO.setUniqueId(root + "^" + extension);
		Element idFullfillment = docCDA.select("ClinicalDocument > inFulfillmentOf > order > id").first();
		if (idFullfillment != null) {
			final String ref = idFullfillment.attr("extension");
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

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

			if (requestBody.getDataInizioPrestazione() != null) {
				de.setServiceStartTime(sdf.parse(requestBody.getDataInizioPrestazione()).toString());
			}

			if (requestBody.getDataFinePrestazione() != null) {
				de.setServiceStopTime(sdf.parse(requestBody.getDataFinePrestazione()).toString());
			}
		} catch(Exception ex) {
			log.error("Error while create document entry : " , ex);
			throw new BusinessException("Error while create document entry : " , ex);
		}
		return de;
	}

}
