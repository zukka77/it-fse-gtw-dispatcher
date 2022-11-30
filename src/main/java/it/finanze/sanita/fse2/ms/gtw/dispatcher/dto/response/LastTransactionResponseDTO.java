/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.StatusCheckDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LastTransactionResponseDTO extends ResponseDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -7747265284310662589L;
	private String transactionStatus;

	@Schema(description = "Ultimo evento trovato")
	private transient StatusCheckDTO lastTransactionData;

	public LastTransactionResponseDTO() {
		super();
		lastTransactionData = null;
	}

	public LastTransactionResponseDTO(final LogTraceInfoDTO traceInfo, final String transactionStatus, final StatusCheckDTO lastTransactionData) {
		super(traceInfo);
		this.transactionStatus = transactionStatus;
		this.lastTransactionData = lastTransactionData;
	}
}
