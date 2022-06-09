package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.LowLevelDocEnum;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.FhirMappingClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.DocumentEntryDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.SubmissionSetEntryDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.DocumentReferenceResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventCodeEnum;
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
	public FhirResourceDTO createFhirResources(final String cda, final PublicationCreationReqDTO requestBody, 
			final Integer size, final String hash) {
		FhirResourceDTO output = new FhirResourceDTO();
		try {
			org.jsoup.nodes.Document docCDA = Jsoup.parse(cda);
			DocumentReferenceResDTO documentReferenceDTO = createDocumentReference(docCDA, requestBody, size, hash);
			
			if(!StringUtility.isNullOrEmpty(documentReferenceDTO.getErrorMessage())) {
				output.setErrorMessage(documentReferenceDTO.getErrorMessage());
			} else {
				output.setDocumentReferenceJson(documentReferenceDTO.getJson());
				
				try {
					SubmissionSetEntryDTO submissionSetEntryDTO = createSubmissionSetEntry(docCDA,requestBody.getTipoAttivitaClinica().getCode(),
							requestBody.getIdentificativoSottomissione());
					output.setSubmissionSetEntryJson(StringUtility.toJSON(submissionSetEntryDTO));
				} catch(Exception ex) {
					output.setErrorMessage(ex.getCause().getCause().getMessage());
				}
				
				if(StringUtility.isNullOrEmpty(documentReferenceDTO.getErrorMessage())) {
					try {
						DocumentEntryDTO documentEntryDTO = createDocumentEntry(docCDA, requestBody, size, hash);
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
	
	private DocumentReferenceResDTO createDocumentReference(final org.jsoup.nodes.Document docCDA, final PublicationCreationReqDTO requestBody, 
			final Integer size, final String hash) {
		DocumentReferenceResDTO out = null;
		try {
			DocumentReferenceDTO documentReferenceDTO = extractedInfoForDocumentReference(docCDA);
			documentReferenceDTO.setSize(size);
			documentReferenceDTO.setHash(hash);
			documentReferenceDTO.setFacilityTypeCode(requestBody.getTipologiaStruttura().getCode());
			
			if(requestBody.getRegoleAccesso()!=null && !requestBody.getRegoleAccesso().isEmpty()) {
				documentReferenceDTO.setEventCode(requestBody.getRegoleAccesso().stream().map(e->e.getCode()).collect(Collectors.toList()));
			}
			documentReferenceDTO.setPracticeSettingCode(requestBody.getAssettoOrganizzativo().getDescription());
			documentReferenceDTO.setPatientID(requestBody.getIdentificativoPaziente());
			documentReferenceDTO.setTipoDocumentoLivAlto(requestBody.getTipoDocumentoLivAlto().getCode());
			documentReferenceDTO.setRepositoryUniqueID(requestBody.getIdentificativoRep());
			documentReferenceDTO.setServiceStartTime(requestBody.getDataInizioPrestazione());
			documentReferenceDTO.setServiceStopTime(requestBody.getDataInizioPrestazione());
			out = client.callCreateDocumentReference(documentReferenceDTO);
		} catch(ConnectionRefusedException crex) {
			throw crex;
		} catch(Exception ex) {
			log.error("Error while create document reference request " , ex);
			throw new BusinessException("Error while create document reference request " , ex);
		}
		return out;
	}

	private DocumentReferenceDTO extractedInfoForDocumentReference(final org.jsoup.nodes.Document docCDA) {
		DocumentReferenceDTO documentReferenceDTO = DocumentReferenceDTO.builder()
				.formatCode(docCDA.select("Clinicaldocument > templateId").first().attr("root"))
				.referencedID(docCDA.select("ClinicalDocument > inFulfillmentOf > order > id").first().attr("extension"))
				.securityLabel(docCDA.select("ClinicalDocument > confidentialityCode").first().attr("code"))
				.masterIdentifier(docCDA.select("ClinicalDocument > id").first().attr("extension"))
				.typeCode(docCDA.select("ClinicalDocument > code").first().attr("code"))
				.author(docCDA.select("ClinicalDocument > author > assignedAuthor > id").first().attr("extension"))
				.authenticator(docCDA.select("ClinicalDocument > legalAuthenticator > assignedEntity > id").first().attr("extension"))
				.custodian(docCDA.select("ClinicalDocument > custodian > assignedCustodian > representedCustodianOrganization > id").first().attr("extension"))
				.build();
		return documentReferenceDTO;
	}
	
	private SubmissionSetEntryDTO createSubmissionSetEntry(final org.jsoup.nodes.Document docCDA, 
			final String contentTypeCode, String identificativoSottomissione) {
		SubmissionSetEntryDTO sse = null;
		
		sse = extractedInfoForSubmissionSetEntry(docCDA,identificativoSottomissione);
		sse.setSubmissionTime(new Date());
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
		///TODO: check
		String sourceIdRoot = docCDA.select("ClinicalDocument > custodian > assignedCustodian > representedCustodianOrganization > id").first().attr("root");
		String sourceIdExtension = docCDA.select("ClinicalDocument > custodian > assignedCustodian > representedCustodianOrganization > id").first().attr("extension");
		sse.setSourceId(sourceIdRoot + "." + sourceIdExtension);
		sse.setUniqueID(identificativoSottomissione);
		return sse;
	}

	
	private DocumentEntryDTO extractedInfoForDocumentEntry(org.jsoup.nodes.Document docCDA) {
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
			documentEntryDTO.setFormatCode(null);
		}
		String legalAuth = docCDA.select("ClinicalDocument > legalAuthenticator > assignedEntity > id").first().attr("extension");
		documentEntryDTO.setLegalAuthenticator(legalAuth);
		String sourcePatientInfo = docCDA.select("ClinicalDocument > recordTarget > patientRole").first().text();
		documentEntryDTO.setSourcePatientInfo(sourcePatientInfo);
		String authorPerson = docCDA.select("ClinicalDocument > author").first().attr("extension");
		documentEntryDTO.setAuthor(authorPerson);
		String representedOrganizationName = docCDA.select("ClinicalDocument > documentationOf > serviceEvent > performer > assignedEntity > representedOrganization > name").first().text();
		String representedOrganizationCode = docCDA.select("ClinicalDocument > documentationOf > serviceEvent > performer > assignedEntity > representedOrganization > asOrganizationPartOf > id").first().attr("extension");
		documentEntryDTO.setRepresentedOrganizationName(representedOrganizationName);
		documentEntryDTO.setRepresentedOrganizationCode(representedOrganizationCode);

		String uniqueId = docCDA.select("ClinicalDocument > id").first().attr("extension");
		///TODO: check if clinicalDocument > id > root is the proper path
		documentEntryDTO.setUniqueId(uniqueId);
		String ref = docCDA.select("ClinicalDocument > inFulfillmentOf > order > id").first().attr("extension");
		List<String> refID = new ArrayList<>();
		refID.add(ref);
		documentEntryDTO.setReferenceIdList(refID);
		return documentEntryDTO;
	}
	
	private DocumentEntryDTO createDocumentEntry(final org.jsoup.nodes.Document docCDA ,
			final PublicationCreationReqDTO requestBody,final Integer size, final String hash) {

		DocumentEntryDTO de = null;
		try {
			de = extractedInfoForDocumentEntry(docCDA);

			de.setMimeType("application/pdf + text/x-cda-r2+xml");
			de.setEntryUUID("Document01");
			de.setCreationTime(new Date());
			de.setSize(size);
			de.setHash(hash);
			de.setStatus("approved");
			de.setLanguageCode("it-IT");
			de.setHealthcareFacilityTypeCode(requestBody.getTipologiaStruttura().getCode());
			de.setHealthcareFacilityTypeCodeName(requestBody.getTipologiaStruttura().getCode());
			if (!CollectionUtils.isEmpty(requestBody.getRegoleAccesso())) {
				de.setEventCodeList(requestBody.getRegoleAccesso().stream().map(EventCodeEnum::getCode).collect(Collectors.toList()));
			}
			de.setRepositoryUniqueId(requestBody.getIdentificativoRep());
			de.setClassCode(requestBody.getTipoDocumentoLivAlto().getCode());
			de.setClassCodeName(requestBody.getTipoDocumentoLivAlto().getDescription());
			de.setPracticeSettingCode(requestBody.getAssettoOrganizzativo().name());
			de.setPracticeSettingCodeName(requestBody.getAssettoOrganizzativo().getDescription());
			de.setSourcePatientId(requestBody.getIdentificativoPaziente());

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

			if (requestBody.getDataInizioPrestazione() != null) {
				de.setServiceStartTime(sdf.parse(requestBody.getDataInizioPrestazione()));
			}

			if (requestBody.getDataFinePrestazione() != null) {
				de.setServiceStopTime(sdf.parse(requestBody.getDataFinePrestazione()));
			}
		} catch(Exception ex) {
			log.error("Error while create document entry : " , ex);
			throw new BusinessException("Error while create document entry : " , ex);
		}
		return de;
	}
}
