/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import java.util.Date;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ILogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ResultLogEnum;

/** 
 * @author: vincenzoingenito 
 */ 

public interface ILogSRV {
 
	public void trace(String message, ILogEnum operation, ResultLogEnum result, Date startDateOperation,String issuer);

	public void debug(String message, ILogEnum operation, ResultLogEnum result, Date startDateOperation, String issuer);

	public void info(String message, ILogEnum operation, ResultLogEnum result, Date startDateOperation, String issuer);

	public void warn(String message, ILogEnum operation, ResultLogEnum result, Date startDateOperation,String issuer);

	public void error(String message, ILogEnum operation, ResultLogEnum result, Date startDateOperation,ILogEnum error, String issuer);


}
