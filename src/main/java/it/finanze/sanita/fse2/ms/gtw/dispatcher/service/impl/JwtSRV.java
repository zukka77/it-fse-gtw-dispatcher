/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.impl.AnaClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PurposeOfUseEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.SubjectOrganizationEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IJwtSRV;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtSRV extends AbstractService implements IJwtSRV {
	
	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 1264747664221422792L;

	@Autowired
	private MicroservicesURLCFG urlCfg;

	@Autowired
	private AnaClient anaCall;

	@Autowired
	private UtilitySRV utilitySrv;
	

	@Override
	public String validatePayload(JWTPayloadDTO payload) {
		String error = null;

		if (payload == null) {
			error = "Il payload del JWT non è valido";
		} else if (!isValidOid(payload.getSub(),false)) {
			error = "Codice fiscale nel campo subject non valido";
		} else if (!isValidOid(payload.getPerson_id(),urlCfg.getAnaEnableValidation())) {
			error = "Codice fiscale nel campo person non valido";
		} else if (SubjectOrganizationEnum.getCode(payload.getSubject_organization_id())==null) {
			error = "Campo subject organization id non valido";
		} else if (SubjectOrganizationEnum.getDisplay(payload.getSubject_organization())==null) {
			error = "Campo subject organization description non valido";
		} else if(!SubjectOrganizationEnum.getCode(payload.getSubject_organization_id()).equals(SubjectOrganizationEnum.getDisplay(payload.getSubject_organization()))){
			error = "I campi subject organization e descrizione non sono concordi";
		} else if (StringUtils.isEmpty(payload.getLocality())) {
			error = "Campo locality non valido";
		} else if (StringUtils.isEmpty(payload.getSubject_role())) {
			error = "Campo subject role non valido";
		} else if (PurposeOfUseEnum.get(payload.getPurpose_of_use())==null) {
			error = "Campo purpose of use non valido";
		} else if (!ActionEnum.isAllowed(payload.getAction_id())) {
			error = "Azione non permessa";
		} else if (StringUtils.isEmpty(payload.getIss())) {
			error = "Campo issuer non valido";
		} else if (StringUtils.isEmpty(payload.getJti())) {
			error = "Campo jti non valido";
		} else if (!Boolean.TRUE.equals(payload.getPatient_consent())) {
			error = "Il consenso del paziente è obbligatorio";
		}

		return error;
	}
	
	private boolean isValidOid(String rawOid,boolean callAna) {
		boolean out = false;
		
		if (rawOid != null) {
			final String [] chunks = rawOid.split("\\^\\^\\^");
				if (chunks.length > 1) {
					log.debug("Searching oid");
					final String[] chunkedInfo = chunks[1].split("&amp;");
					if (chunkedInfo.length > 1 && Constants.OIDS.OID_MEF.equals(chunkedInfo[1])) {
						if(callAna) {
							out = anaCall.callAnaClient(chunks[0]);
						} else {
							out = utilitySrv.isValidCf(chunks[0]);
						}
					} else {
						out = true;
					}
				} else {
					if(callAna) {
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

		private static boolean isAllowed(final String value) {

			for (ActionEnum v : ActionEnum.values()) {
				if (v.name().equals(value)) {
					return true;
				}
			}
			return false;
		}
	}
	
}
