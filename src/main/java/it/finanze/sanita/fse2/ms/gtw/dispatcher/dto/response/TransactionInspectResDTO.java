/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import java.util.List;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.StatusCheckDTO;
import lombok.Getter;
import lombok.Setter;

/**
 *	DTO used to return inspect result.
 */
@Getter
@Setter
public class TransactionInspectResDTO extends ResponseDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -1550024371939901939L;
	
	private List<StatusCheckDTO> transactionData;
	
	public TransactionInspectResDTO() {
		super();
		transactionData = null;
	}

	public TransactionInspectResDTO(final LogTraceInfoDTO traceInfo, final List<StatusCheckDTO> inTransactionData) {
		super(traceInfo);
		transactionData = inTransactionData;
	}
	
}
