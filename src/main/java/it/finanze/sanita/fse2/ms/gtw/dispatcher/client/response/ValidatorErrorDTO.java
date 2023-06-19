/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client.response;

import lombok.Data;

@Data
public class ValidatorErrorDTO {

    @Data
    public static class Payload {
        private Integer code;
        private String message;
    }

    private String traceID;
    private String spanID;
    private Payload error;
}
