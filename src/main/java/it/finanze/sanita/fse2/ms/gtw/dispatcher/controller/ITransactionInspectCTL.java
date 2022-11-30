/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Size;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TransactionInspectResDTO;

/**
*
* Controller for Transactions' Inspection.
*/
@RequestMapping(path = "/v1")
@Tag(name = "Servizio ispezione transazioni")
public interface ITransactionInspectCTL {

	@GetMapping(value = "/{workflowInstanceId}")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransactionInspectResDTO.class)))
	@Operation(summary = "Recupero eventi del workflow instance id", description = "Recupera il dettaglio degli eventi tramite il workflow instance id.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Eventi recuperati", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransactionInspectResDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "Record not found", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	TransactionInspectResDTO getEvents(@PathVariable(required = true, name = "workflowInstanceId") @Schema(description = "Identificativo del workflow") @Size(min = 0, max = 1000) String workflowInstanceId,HttpServletRequest request);

 
	@GetMapping(value = "/search/{traceId}")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransactionInspectResDTO.class)))
	@Operation(summary = "Recupero eventi del trace id", description = "Recupera il dettaglio degli eventi tramite il trace id.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Eventi recuperati", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransactionInspectResDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "Record not found", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	TransactionInspectResDTO getEventsByTraceId(@PathVariable(required = true, name = "traceId") String traceId,HttpServletRequest request);
	
}
