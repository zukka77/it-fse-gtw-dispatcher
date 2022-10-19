/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import java.util.List;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RawValidationEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationInfoDTO {

	private RawValidationEnum result;
	
	private List<String> message;
}
