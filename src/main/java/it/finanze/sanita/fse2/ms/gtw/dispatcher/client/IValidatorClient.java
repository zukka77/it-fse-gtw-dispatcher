/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationInfoDTO;

/**
 * Interface of Validator Client.
 */
public interface IValidatorClient {
	String SYSTEM_TYPE_HEADER = "X-System-Type";
	ValidationInfoDTO validate(String cda, String workflowInstanceId, String system);
}
