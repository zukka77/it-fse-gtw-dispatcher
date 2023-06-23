/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

@Getter
public enum DescriptionEnum {

	DESC_OID1("2.16.840.1.113883.2.9.6.1.5"),
	DESC_OID2("2.16.840.1.113883.2.9.6.1.51"),
	DESC_OID3("2.16.840.1.113883.6.73"),
	DESC_OID4("2.16.840.1.113883.2.9.6.1.11"),
	DESC_OID5("2.16.840.1.113883.2.9.2.COD_REGIONE.6.1.11"),
	DESC_OID6("2.16.840.1.113883.2.9.6.1.5"),
	DESC_OID7("2.16.840.1.113883.6.73");

	private String oid;
	
	private DescriptionEnum(String inOid) {
		oid = inOid;
	}
}
