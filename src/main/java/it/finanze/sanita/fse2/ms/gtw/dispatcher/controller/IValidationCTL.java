/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ValidationErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ValidationResDTO;

/**
 * Controller validation.
 */

@RequestMapping(path = "/v1")
@Tag(name = "Servizio validazione documenti")
public interface IValidationCTL {

	
	@RequestMapping(value = "/documents/validation", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidationResDTO.class)))
	@Operation(summary = "Validazione documenti", description = "Valida il CDA iniettato nel PDF fornito in input.")
	@SecurityRequirements({
		@SecurityRequirement(name = "bearerAuth"),
		@SecurityRequirement(name = "FSE-JWT-Signature")}) 
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Validazione positiva a seguito di activity verifica", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidationResDTO.class))),
			@ApiResponse(responseCode = "201", description = "Validazione positiva a seguito di activity validation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidationResDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
			@ApiResponse(responseCode = "403", description = "Token jwt mancante o non valido", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
			@ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
			@ApiResponse(responseCode = "413", description = "Payload too large", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
			@ApiResponse(responseCode = "415", description = "Unsupported media type", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
			@ApiResponse(responseCode = "422", description = "Richiesta semanticamente non processabile", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
			@ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
			@ApiResponse(responseCode = "502", description = "Invalid response received from the API Implementation", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
			@ApiResponse(responseCode = "503", description = "Service unavailable", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
			@ApiResponse(responseCode = "504", description = "Endpoint request timed-out", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponseDTO.class)))})
	ResponseEntity<ValidationResDTO> validate(@RequestBody(required = false) ValidationCDAReqDTO requestBody, @RequestPart("file") MultipartFile file, HttpServletRequest request);

}
