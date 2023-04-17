package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventTypeEnum;

public interface ISignSRV {

	void checkPades(final byte[] pdf,EventTypeEnum eventTypeEnum);
}
