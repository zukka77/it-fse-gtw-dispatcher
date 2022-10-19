/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PublicationOutputDTO {
	private String msg;
    private RestExecutionResultEnum result;
}
