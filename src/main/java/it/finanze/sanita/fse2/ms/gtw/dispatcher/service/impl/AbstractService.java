/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import brave.Tracer;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;

/**
 * Abstract service
 */
@Service
public abstract class AbstractService implements Serializable {

	/**
	 * Serial.
	 */
	private static final long serialVersionUID = 1L;

	@Autowired
	private transient Tracer tracer;

	protected LogTraceInfoDTO getLogTraceInfo() {
		LogTraceInfoDTO out = new LogTraceInfoDTO(null, null);
		if (tracer.currentSpan() != null) {
			out = new LogTraceInfoDTO(tracer.currentSpan().context().spanIdString(),
					tracer.currentSpan().context().traceIdString());
		}
		return out;
	}

}