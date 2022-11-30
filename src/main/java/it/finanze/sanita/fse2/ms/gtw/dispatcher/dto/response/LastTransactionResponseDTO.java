/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.StatusCheckDTO;
import lombok.Data;

public class LastTransactionResponseDTO extends ResponseDTO {

	private String transactionStatus;

	private StatusCheckDTO lastTransactionData;

	public LastTransactionResponseDTO() {
		super();
		lastTransactionData = null;
	}

	public LastTransactionResponseDTO(final LogTraceInfoDTO traceInfo, final String inTransactionStatus, final StatusCheckDTO inLastTransactionData) {
		super(traceInfo);
		transactionStatus = inTransactionStatus;
		lastTransactionData = inLastTransactionData;
	}

	public String getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	public StatusCheckDTO getLastTransactionData() {
		return lastTransactionData;
	}

	public void setLastTransactionData(StatusCheckDTO lastTransactionData) {
		this.lastTransactionData = lastTransactionData;
	}
	
	
}
