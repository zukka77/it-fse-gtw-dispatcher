/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import javax.servlet.http.HttpServletRequest;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Hidden;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.ITSFeedingCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.TSPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TSPublicationCreationResDTO;

/**
 * TS document feeding controller.
 */
@RestController
@Hidden
public class TSFeedingCTL extends AbstractCTL implements ITSFeedingCTL {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -8596007149594204537L;

	@Override
	public TSPublicationCreationResDTO tsFeeding(final TSPublicationCreationReqDTO requestBody,
			final MultipartFile file, final HttpServletRequest request) {

		return new TSPublicationCreationResDTO();
	}
}
