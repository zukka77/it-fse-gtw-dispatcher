/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.ValidationCFG;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CfUtility;

@Service
public class UtilitySRV extends AbstractService {
	
	@Autowired
	private transient ValidationCFG validationCfg;

	public boolean isValidCf(final String fiscalCode) {
		boolean out = false;

		if (fiscalCode != null) {
			if (Boolean.TRUE.equals(validationCfg.getAllowSpecialFiscalCodes())) {
				out = CfUtility.validaCF(fiscalCode) == CfUtility.CF_OK_16
						|| CfUtility.validaCF(fiscalCode) == CfUtility.CF_OK_11
						|| CfUtility.validaCF(fiscalCode) == CfUtility.CF_ENI_OK
						|| CfUtility.validaCF(fiscalCode) == CfUtility.CF_STP_OK;
			} else {
				out = (fiscalCode.length() == 16 && CfUtility.validaCF(fiscalCode) == CfUtility.CF_OK_16);
			}
		} else {
			out = false;
		}
		return out;
	}
}
