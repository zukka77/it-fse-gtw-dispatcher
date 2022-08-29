/**
 * 
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.DeleteRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.IniMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniTraceResponseDTO;

/**
 * @author AndreaPerquoti
 *
 */
public interface IIniClient extends Serializable {
	
	IniTraceResponseDTO delete(DeleteRequestDTO iniReq);
	
	IniTraceResponseDTO updateMetadati(IniMetadataUpdateReqDTO iniReq);

}
