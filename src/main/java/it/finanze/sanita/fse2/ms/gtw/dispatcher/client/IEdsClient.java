/**
 * 
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.EdsTraceResponseDTO;

import java.io.Serializable;

/**
 * @author AndreaPerquoti
 *
 */
public interface IEdsClient extends Serializable {

	EdsTraceResponseDTO delete(final String oid);
	EdsTraceResponseDTO update(final EdsMetadataUpdateReqDTO req);

}
