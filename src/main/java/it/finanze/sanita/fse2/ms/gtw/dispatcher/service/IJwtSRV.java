/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;

public interface IJwtSRV extends Serializable {

	String validatePayload(JWTPayloadDTO payload); 
	
	
	
}
