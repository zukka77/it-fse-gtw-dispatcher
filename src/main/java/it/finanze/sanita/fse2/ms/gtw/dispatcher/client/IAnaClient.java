/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client;

import java.io.Serializable;


public interface IAnaClient extends Serializable{
	
	Boolean callAnaClient(String codFiscale);
}
