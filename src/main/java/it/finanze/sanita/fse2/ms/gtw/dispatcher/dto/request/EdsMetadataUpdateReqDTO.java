/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EdsMetadataUpdateReqDTO {
    private String idDoc;
    private String workflowInstanceId;
    private PublicationMetadataReqDTO body;
}
