/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.DeleteRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.IniMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.IniReferenceRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.MergedMetadatiRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.GetMergedMetadatiDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniReferenceResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.IniTraceResponseDTO;

public interface IIniClient {
	
	IniTraceResponseDTO delete(DeleteRequestDTO iniReq);
	
	IniTraceResponseDTO update(IniMetadataUpdateReqDTO iniReq);

	IniReferenceResponseDTO reference(it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.IniReferenceRequestDTO iniReferenceRequestDTO);
	
	GetMergedMetadatiDTO metadata(MergedMetadatiRequestDTO iniReq);

}
