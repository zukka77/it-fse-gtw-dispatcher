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
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.FhirMappingClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.*;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreateReplaceMetadataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.TransformResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.LowLevelDocEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IDocumentReferenceSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

@Service
@Slf4j
public class DocumentReferenceSRV implements IDocumentReferenceSRV {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 6613399511662450678L;
	private static final String PATH_CUSTODIAN_ID = "ClinicalDocument > custodian > assignedCustodian > representedCustodianOrganization > id";
	private static final String PATH_PATIENT_ID = "ClinicalDocument > recordTarget > patientRole> id";
	private static final String ERROR_MSG = "Error while create document reference request ";
	private static final String EXTENSION_ATTRIBUTE = "extension";

	@Autowired
	private FhirMappingClient client;

	@Override
	public ResourceDTO createFhirResources(final String cda,
			String authorRole,final PublicationCreateReplaceMetadataDTO requestBody,
			final Integer size, final String hash, String transformId, String engineId) {
		final ResourceDTO output = new ResourceDTO();
		try {
			final org.jsoup.nodes.Document docCDA = Jsoup.parse(cda);
			final String encodedCDA = Base64.getEncoder().encodeToString(cda.getBytes());

			final DocumentReferenceDTO documentReferenceDTO = buildDocumentReferenceDTO(encodedCDA, requestBody, size, hash);

			final TransformResDTO resDTO = callFhirMapping(documentReferenceDTO, cda, transformId, engineId);

			if (!StringUtility.isNullOrEmpty(resDTO.getErrorMessage())) {
				output.setErrorMessage(resDTO.getErrorMessage());
			} else {
				output.setBundleJson(StringUtility.toJSON(resDTO.getJson()));

				try {
					final SubmissionSetEntryDTO submissionSetEntryDTO = createSubmissionSetEntry(docCDA, requestBody.getTipoAttivitaClinica().getCode(),
							requestBody.getIdentificativoSottomissione());
					output.setSubmissionSetEntryJson(StringUtility.toJSON(submissionSetEntryDTO));
				} catch(final Exception ex) {
					output.setErrorMessage(ex.getCause().getCause().getMessage());
				}

				if(StringUtility.isNullOrEmpty(resDTO.getErrorMessage())) {
					try {
						final DocumentEntryDTO documentEntryDTO = createDocumentEntry(docCDA, requestBody, size, hash,authorRole);
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

	private DocumentReferenceDTO buildDocumentReferenceDTO(final String encodedCDA, final PublicationCreateReplaceMetadataDTO requestBody,
			final Integer size, final String hash) {
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
			documentReferenceDTO.setTipoDocumentoLivAlto(requestBody.getTipoDocumentoLivAlto().getCode());
			documentReferenceDTO.setRepositoryUniqueID(requestBody.getIdentificativoRep());
			documentReferenceDTO.setServiceStartTime(requestBody.getDataInizioPrestazione());
			documentReferenceDTO.setServiceStopTime(requestBody.getDataFinePrestazione());
			documentReferenceDTO.setIdentificativoDoc(requestBody.getIdentificativoDoc());
		} catch(final Exception ex) {
			log.error(ERROR_MSG , ex);
			throw new BusinessException(ERROR_MSG , ex);
		}
		return documentReferenceDTO;
	}

	private TransformResDTO callFhirMapping(final DocumentReferenceDTO documentReferenceDTO, final String cda,
			String transformId, String engineId) {
		TransformResDTO out = null;
		try {

			final FhirResourceDTO req = new FhirResourceDTO();
			req.setCda(cda);
			req.setDocumentReferenceDTO(documentReferenceDTO); 
			req.setObjectId(transformId);
			req.setEngineId(engineId);

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

		SubmissionSetEntryDTO sse = new SubmissionSetEntryDTO();


		String sourceIdRoot = "";
		final Element custodianPath = docCDA.select(PATH_CUSTODIAN_ID).first();
		if (custodianPath != null) {
			sourceIdRoot = custodianPath.attr("root");
		}

		sse.setSourceId(sourceIdRoot);
		sse.setUniqueID(identificativoSottomissione);

		sse.setSubmissionTime(new SimpleDateFormat(Constants.Misc.INI_DATE_PATTERN).format(new Date()));
		sse.setContentTypeCode(contentTypeCode);
		final AttivitaClinicaEnum contentTypeCodeName = Arrays.stream(AttivitaClinicaEnum.values()).filter(attivitaClinicaEnum -> attivitaClinicaEnum.getCode().equals(contentTypeCode)).findFirst().orElse(null);
		if (contentTypeCodeName != null) {
			sse.setContentTypeCodeName(contentTypeCodeName.getDescription());
		} else {
			sse.setContentTypeCodeName(null);
		}
		return sse;
	}


	private DocumentEntryDTO createDocumentEntry(final org.jsoup.nodes.Document docCDA,
			final PublicationCreateReplaceMetadataDTO requestBody, final Integer size, final String hash,
			String authorRole) {

		DocumentEntryDTO de = new DocumentEntryDTO();
		try {

			final Element patientIdElement = docCDA.select(PATH_PATIENT_ID).first();
			if (patientIdElement != null) {
				de.setPatientId(patientIdElement.attr(EXTENSION_ATTRIBUTE));
			}

			final Element confidentialityElement = docCDA.select("ClinicalDocument > confidentialityCode").first();
			if (confidentialityElement != null) {
				de.setConfidentialityCode(confidentialityElement.attr("code"));
				de.setConfidentialityCodeDisplayName(confidentialityElement.attr("displayName"));
			}

			final Element typeCodeElement = docCDA.select("ClinicalDocument > code").first();
			if (typeCodeElement != null) {
				de.setTypeCode(typeCodeElement.attr("code"));
				de.setTypeCodeName(typeCodeElement.attr("displayName"));
			}

			final Element formatCodeElement = docCDA.select("ClinicalDocument > templateId").first();
			if (formatCodeElement != null) {
				final String formatCode = formatCodeElement.attr("root");
				de.setFormatCode(formatCode);
				final LowLevelDocEnum formatCodeEnum = Arrays.stream(LowLevelDocEnum.values()).filter(lowLevelDocEnum -> lowLevelDocEnum.getCode().equals(formatCode)).findFirst().orElse(null);
				if (formatCodeEnum != null) {
					de.setFormatCodeName(formatCodeEnum.getDescription());
				} else {
					de.setFormatCodeName(null);
				}
			}

			de.setUniqueId(requestBody.getIdentificativoDoc());

			de.setMimeType("application/pdf+text/x-cda-r2+xml");
			de.setCreationTime(new SimpleDateFormat(Constants.Misc.INI_DATE_PATTERN).format(new Date()));
			de.setHash(hash);
			de.setSize(size);
			if(requestBody.getAdministrativeRequest() != null) {
				de.setAdministrativeRequest(requestBody.getAdministrativeRequest().getCode());
			}

			de.setAuthorRole(authorRole);

			final Element authorInstitutionElement = docCDA.select("ClinicalDocument > author > assignedAuthor > representedOrganization > id").first();
			if (authorInstitutionElement != null) {
				String extension = authorInstitutionElement.attr(EXTENSION_ATTRIBUTE);
				String root = authorInstitutionElement.attr("root");
				String assigningAuthorityName = authorInstitutionElement.attr("assigningAuthorityName");
				de.setAuthorInstitution(assigningAuthorityName + "^^^^^&" + root + "&ISO^^^^" + extension);
			} else {
				de.setAuthorInstitution("AUTHOR_INSTITUTION_NOT_PRESENT");
			}

			final Element authorElement = docCDA.select("ClinicalDocument > author > assignedAuthor > id").first();
			if (authorElement != null) {
				de.setAuthor(authorElement.attr(EXTENSION_ATTRIBUTE));
			}
			de.setFirma("true^Documento firmato");

			de.setLanguageCode("it-IT");
			de.setRepositoryUniqueId(requestBody.getIdentificativoRep());

			if(requestBody.getDescriptions() != null) {
				de.setDescription(requestBody.getDescriptions());
			}

			de.setHealthcareFacilityTypeCode(requestBody.getTipologiaStruttura().getCode());
			de.setHealthcareFacilityTypeCodeName(requestBody.getTipologiaStruttura().getCode());
			if (!CollectionUtils.isEmpty(requestBody.getAttiCliniciRegoleAccesso())) {
				de.setEventCodeList(requestBody.getAttiCliniciRegoleAccesso());
			}

			de.setClassCode(requestBody.getTipoDocumentoLivAlto().getCode());
			de.setClassCodeName(requestBody.getTipoDocumentoLivAlto().getDescription());
			de.setPracticeSettingCode(requestBody.getAssettoOrganizzativo().name());
			de.setPracticeSettingCodeName(requestBody.getAssettoOrganizzativo().getDescription());

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
