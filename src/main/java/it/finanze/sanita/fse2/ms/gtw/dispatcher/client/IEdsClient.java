/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.EdsResponseDTO;

public interface IEdsClient {

	EdsResponseDTO delete(final String oid);
	
	EdsResponseDTO update(final EdsMetadataUpdateReqDTO req);

}
