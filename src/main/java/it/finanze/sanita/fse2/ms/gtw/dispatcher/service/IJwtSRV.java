/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;

public interface IJwtSRV {

	/**
	 * Validate the payload of the JWT token for the create operation.
	 * 
	 * @param payload The payload of the JWT token.
	 */
	void validatePayloadForCreate(JWTPayloadDTO payload);

	/**
	 * Validate the payload of the JWT token for the update operation.
	 * 
	 * @param payload The payload of the JWT token.
	 */
	void validatePayloadForUpdate(JWTPayloadDTO payload);

	/**
	 * Validate the payload of the JWT token for the delete operation.
	 * 
	 * @param payload The payload of the JWT token.
	 */
	void validatePayloadForDelete(JWTPayloadDTO payload);

	/**
	 * Validate the payload of the JWT token for the replace operation.
	 * 
	 * @param payload The payload of the JWT token.
	 */
	void validatePayloadForReplace(JWTPayloadDTO payload);

	/**
	 * Validate the payload of the JWT token for the feeding operation.
	 * 
	 * @param payload The payload of the JWT token.
	 */
	void validatePayloadForValidation(JWTPayloadDTO payload);
}
