/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

public enum OperationLogEnum implements ILogEnum {

	VAL_CDA2("VAL-CDA2", "Validazione CDA2"),
	PUB_CDA2("PUB-CDA2", "Pubblicazione CDA2"),
	REPLACE_CDA2("REPLACE-CDA2", "Replace CDA2"),
	DELETE_CDA2("DELETE-CDA2","Delete CDA2"),
	UPDATE_METADATA_CDA2("UPDATE-CDA2","Update metadata CDA2"),
	TRAS_CDA2("TRAS-CDA2", "Trasmissione CDA2"),
	KAFKA_SENDING_MESSAGE("KAFKA-SENDING-MESSAGE", "Invio Messaggio su Kafka"),
	MONGO("MONGO", "Salvataggio/Query su Mongo");

	@Getter
	private String code;

	@Getter
	private String description;

	private OperationLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}
