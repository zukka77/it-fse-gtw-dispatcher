/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.JwtCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActionEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ErrorInstanceEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PurposeOfUseEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RoleEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.SubjectOrganizationEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IJwtSRV;

@Service
public class JwtSRV extends AbstractService implements IJwtSRV {
	
	@Autowired
	private UtilitySRV utilitySrv;
	
	@Autowired
	private JwtCFG jwtCFG;
	
	@Override
	public void validatePayloadForValidation(JWTPayloadDTO payload) {
		performCommonValidation(payload);
		checkNull(payload.getPatient_consent(), "patient_consent");
		checkNull(payload.getResource_hl7_type(), "resource_hl7_type");
		validateActionCoherence(payload, ActionEnum.CREATE);
		validatePurposeOfUseCoherence(payload, PurposeOfUseEnum.TREATMENT);
	}
	
	@Override
	public void validatePayloadForCreate(JWTPayloadDTO payload) {
		performCommonValidation(payload);
		checkNull(payload.getPatient_consent(), "patient_consent");
		checkNull(payload.getResource_hl7_type(), "resource_hl7_type");
		validateActionCoherence(payload, ActionEnum.CREATE);
		validatePurposeOfUseCoherence(payload, PurposeOfUseEnum.TREATMENT);
	}

	@Override
	public void validatePayloadForReplace(JWTPayloadDTO payload) {
		performCommonValidation(payload);
		checkNull(payload.getPatient_consent(), "patient_consent");
		checkNull(payload.getResource_hl7_type(), "resource_hl7_type");
		validateActionCoherence(payload, ActionEnum.CREATE);
		validatePurposeOfUseCoherence(payload, PurposeOfUseEnum.TREATMENT);
	}
	
	@Override
	public void validatePayloadForUpdate(JWTPayloadDTO payload) {
		performCommonValidation(payload);
		checkNull(payload.getPatient_consent(), "patient_consent");
		validateActionCoherence(payload, ActionEnum.UPDATE);
		validatePurposeOfUseCoherence(payload, PurposeOfUseEnum.UPDATE);
	}

	@Override
	public void validatePayloadForDelete(JWTPayloadDTO payload) {
		performCommonValidation(payload);
		validateActionCoherence(payload, ActionEnum.DELETE);
		validatePurposeOfUseCoherence(payload, PurposeOfUseEnum.UPDATE);
	}

	private void performCommonValidation(JWTPayloadDTO payload) {
		validateMandatoryFields(payload);
		validateFiscalCodes(payload);
		validateFieldsValue(payload);
	}
	
	public void validateMandatoryFields(JWTPayloadDTO payload) {
		checkNull(payload, "jwt payload");
		checkNull(payload.getAction_id(), "action_id");
		checkNull(payload.getLocality(), "locality");
		checkNull(payload.getPerson_id(), "person_id");
		checkNull(payload.getPurpose_of_use(), "purpose_of_use");
		checkNull(payload.getSubject_organization(), "subject_organization");
		checkNull(payload.getSubject_organization_id(), "subject_organization_id");
		checkNull(payload.getSubject_role(), "subject_role");

		if (jwtCFG.isClaimsRequired()) {
			checkNull(payload.getSubject_application_id(), "subject_application_id");
			checkNull(payload.getSubject_application_vendor(), "subject_application_vendor");
			checkNull(payload.getSubject_application_version(), "subject_application_version");
		}
	}

	private void validateFiscalCodes(JWTPayloadDTO payload) {
		checkFiscalCode(payload.getSub(), "sub");
		checkFiscalCode(payload.getPerson_id(), "person_id");
	}

	private void validateFieldsValue(JWTPayloadDTO payload) {
		checkEnumValue(ActionEnum.get(payload.getAction_id()), payload.getAction_id(), "action_id");
		checkEnumValue(PurposeOfUseEnum.get(payload.getPurpose_of_use()), payload.getPurpose_of_use(), "purpose_of_use");
		checkEnumValue(RoleEnum.get(payload.getSubject_role()), payload.getSubject_role(), "subject_role");

		SubjectOrganizationEnum subjectOrganizationFromId = SubjectOrganizationEnum.getCode(payload.getSubject_organization_id());
		checkEnumValue(subjectOrganizationFromId, payload.getSubject_organization_id(), "subject_organization_id");

		SubjectOrganizationEnum subjectOrganizationFromDescription = SubjectOrganizationEnum.getDisplay(payload.getSubject_organization());
		checkEnumValue(subjectOrganizationFromDescription, payload.getSubject_organization(), "subject_organization");

		validateSubjectOrganizationCoherence(subjectOrganizationFromId, subjectOrganizationFromDescription);
	}
	
	private void validateActionCoherence(JWTPayloadDTO payload, ActionEnum expectedAction) {
		ActionEnum action = ActionEnum.get(payload.getAction_id());
		if (!expectedAction.equals(action)) {
			ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getType())
				.title(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getTitle())
				.instance(ErrorInstanceEnum.JWT_MALFORMED_FIELD.getInstance())
				.detail(String.format("Il campo action_id non coerente con operazione richiesta"))
				.build();

			throw new ValidationException(error);
		}
	}
	
	private void validatePurposeOfUseCoherence(JWTPayloadDTO payload, PurposeOfUseEnum expectedPurpose) {
		PurposeOfUseEnum purpose = PurposeOfUseEnum.get(payload.getPurpose_of_use());
		if (!expectedPurpose.equals(purpose)) {
			ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getType())
				.title(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getTitle())
				.instance(ErrorInstanceEnum.JWT_MALFORMED_FIELD.getInstance())
				.detail(String.format("Il campo purpose_of_use non coerente con operazione richiesta"))
				.build();

			throw new ValidationException(error);
		}
	}

	private void validateSubjectOrganizationCoherence(SubjectOrganizationEnum subjectOrganizationFromId, SubjectOrganizationEnum subjectOrganizationFromDescription) {
		if (!subjectOrganizationFromId.equals(subjectOrganizationFromDescription)) {
			ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getType())
				.title(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getTitle())
				.instance(ErrorInstanceEnum.JWT_MALFORMED_FIELD.getInstance())
				.detail("I campi subject_organization_id e subject_organization non sono coerenti")
				.build();

			throw new ValidationException(error);
		}		
	}

	private void checkEnumValue(Object enumValue, String givenValue, String fieldName) {
		if (enumValue == null) {
			ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getType())
				.title(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getTitle())
				.instance(ErrorInstanceEnum.JWT_MALFORMED_FIELD.getInstance())
				.detail(String.format("Il campo %s non è corretto", fieldName))
				.build();

			throw new ValidationException(error);
		}

	}

	private void checkNull(String value, String fieldName) {
		if (StringUtils.isEmpty(value)) {
			ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getType())
				.title(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getTitle())
				.instance(ErrorInstanceEnum.MISSING_JWT_FIELD.getInstance())
				.detail(String.format("Il campo %s non è valorizzato", fieldName))
				.build();

			throw new ValidationException(error);
		}
	}

	private void checkNull(Object value, String fieldName) {
		if (ObjectUtils.isEmpty(value)) {
			ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getType())
				.title(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getTitle())
				.instance(ErrorInstanceEnum.MISSING_JWT_FIELD.getInstance())
				.detail(String.format("Il campo %s non è valorizzato", fieldName))
				.build();

			throw new ValidationException(error);
		}
	}

	private void checkFiscalCode(String givenValue, String fieldName) {
		if (!isValidOid(givenValue)) {
			ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getType())
				.title(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getTitle())
				.instance(ErrorInstanceEnum.JWT_MALFORMED_FIELD.getInstance())
				.detail(String.format("Il codice fiscale nel campo %s non è corretto", fieldName))
				.build();

			throw new ValidationException(error);
		}
	}
	
	private boolean isValidOid(String oid) {
		if (oid == null) return false;
		
		final String[] chunks = oid.split("\\^\\^\\^");
		if (chunks.length == 0) return false;
		if (chunks.length == 1) return utilitySrv.isValidCf(chunks[0]);
		
		final String[] chunkedInfo = chunks[1].split("&amp;");
		if (chunkedInfo.length > 1 && Constants.OIDS.OID_MEF.equals(chunkedInfo[1])) {
			return utilitySrv.isValidCf(chunks[0]);
		}

		return true;
	}
}
