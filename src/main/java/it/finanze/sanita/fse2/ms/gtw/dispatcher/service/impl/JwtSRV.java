/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.AnaClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.JwtCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ErrorInstanceEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PurposeOfUseEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RoleEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.SubjectOrganizationEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IJwtSRV;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtSRV extends AbstractService implements IJwtSRV {

	@Autowired
	private AnaClient anaCall;

	@Autowired
	private UtilitySRV utilitySrv;

	@Autowired
	private JwtCFG jwtCFG;

	@Override
	public void validatePayloadForCreate(JWTPayloadDTO payload) {
		validateMandatoryFields(payload);
		validateFieldsValue(payload);
		
		validateActionCoherence(payload, ActionEnum.CREATE);
	}

	@Override
	public void validatePayloadForUpdate(JWTPayloadDTO payload) {
		validateMandatoryFields(payload);
		validateFieldsValue(payload);
		
		validateActionCoherence(payload, ActionEnum.UPDATE);
	}

	@Override
	public void validatePayloadForDelete(JWTPayloadDTO payload) {
		validateMandatoryFields(payload);
		validateFieldsValue(payload);
		
		validateActionCoherence(payload, ActionEnum.DELETE);
		// TODO Da verificare patient consent
	}

	@Override
	public void validatePayloadForReplace(JWTPayloadDTO payload) {
		validateMandatoryFields(payload);
		validateFieldsValue(payload);
		validateActionCoherence(payload, ActionEnum.CREATE);
	}

	@Override
	public void validatePayloadForValidation(JWTPayloadDTO payload) {
		validateMandatoryFields(payload);
		validateFieldsValue(payload);

		ActionEnum action = ActionEnum.getFromDescription(payload.getAction_id());
		if (ActionEnum.CREATE.equals(action)) {

		}
		// TODO
	}

	private void validateActionCoherence(JWTPayloadDTO payload, ActionEnum expectedAction) {
		ActionEnum action = ActionEnum.getFromDescription(payload.getAction_id());
		if (!expectedAction.equals(action)) {
			
			ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getType())
				.title(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getTitle())
				.instance(ErrorInstanceEnum.JWT_MALFORMED_FIELD.getInstance())
				.detail(String.format("action_id %s non coerente con operazione richiesta", action))
				.build();

			throw new ValidationException(error);
		}
	}

	private void validateFieldsValue(JWTPayloadDTO payload) {

		checkEnumValue(ActionEnum.getFromDescription(payload.getAction_id()), payload.getAction_id(), "action_id");
		checkEnumValue(PurposeOfUseEnum.get(payload.getPurpose_of_use()), payload.getPurpose_of_use(), "purpose_of_use");
		checkEnumValue(RoleEnum.getFromCode(payload.getSubject_role()), payload.getSubject_role(), "subject_role");

		SubjectOrganizationEnum subjectOrganization = SubjectOrganizationEnum.getCode(payload.getSubject_organization_id());
		checkEnumValue(subjectOrganization, payload.getSubject_organization_id(), "subject_organization_id");

		SubjectOrganizationEnum subjectOrganizationFromDescription = SubjectOrganizationEnum.getDisplay(payload.getSubject_organization());
		checkEnumValue(subjectOrganizationFromDescription, payload.getSubject_organization(), "subject_organization");

		if (!subjectOrganization.equals(subjectOrganizationFromDescription)) {
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
				.detail(String.format("Il valore %s del campo %s non è valorizzato correttamente", givenValue, fieldName))
				.build();

			throw new ValidationException(error);
		}

	}

	public void validateMandatoryFields(JWTPayloadDTO payload) {

		checkNull(payload, "jwt payload");
		checkNull(payload.getAction_id(), "action_id");
		checkNull(payload.getLocality(), "locality");
		checkNull(payload.getPatient_consent(), "patient_consent");
		checkNull(payload.getPerson_id(), "person_id");
		checkNull(payload.getPurpose_of_use(), "purpose_of_use");
		checkNull(payload.getResource_hl7_type(), "resource_hl7_type");
		checkNull(payload.getSubject_organization(), "subject_organization");
		checkNull(payload.getSubject_organization_id(), "subject_organization_id");
		checkNull(payload.getSubject_role(), "subject_role");

		if (jwtCFG.isClaimsRequired()) {
			checkNull(payload.getSubject_application_id(), "subject_application_id");
			checkNull(payload.getSubject_application_vendor(), "subject_application_vendor");
			checkNull(payload.getSubject_application_version(), "subject_application_version");
		}

	}

	private void checkNull(String value, String fieldName) {
		if (StringUtils.isEmpty(value)) {
			ErrorResponseDTO error = ErrorResponseDTO.builder()
				.type(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getType())
				.title(RestExecutionResultEnum.INVALID_TOKEN_FIELD.getTitle())
				.instance(ErrorInstanceEnum.MISSING_JWT_FIELD.getInstance())
				.detail(String.format("Il campo %s deve essere valorizzato", fieldName))
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
				.detail(String.format("Il campo %s deve essere valorizzato", fieldName))
				.build();

			throw new ValidationException(error);
		}
	}

	// TODO Aggiungere questa validazione
	private boolean isValidOid(String rawOid, boolean callAna) {
		boolean out = false;

		if (rawOid != null) {
			final String[] chunks = rawOid.split("\\^\\^\\^");
			if (chunks.length > 1) {
				log.debug("Searching oid");
				final String[] chunkedInfo = chunks[1].split("&amp;");
				if (chunkedInfo.length > 1 && Constants.OIDS.OID_MEF.equals(chunkedInfo[1])) {
					if (callAna) {
						out = anaCall.callAnaClient(chunks[0]);
					} else {
						out = utilitySrv.isValidCf(chunks[0]);
					}
				} else {
					out = true;
				}
			} else {
				if (callAna) {
					out = anaCall.callAnaClient(chunks[0]);
				} else {
					out = utilitySrv.isValidCf(chunks[0]);
				}
			}
		} else {
			out = false;
		}

		return out;
	}

	private enum ActionEnum {

		CREATE, READ, UPDATE, DELETE;
	
		public static ActionEnum getFromDescription(String action_id) {
			for (ActionEnum v : ActionEnum.values()) {
				if (v.name().equals(action_id)) {
					return v;
				}
			}
			return null;
		}
	}

}
