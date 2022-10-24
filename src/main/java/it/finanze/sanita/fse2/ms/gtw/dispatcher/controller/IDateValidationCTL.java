package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidatedDocumentsDTO;


@RequestMapping(path = "/v1")
@Tag(name = "Servizio modifica data documenti validati")
public interface IDateValidationCTL {

	
	@RequestMapping(value = "/documents/updateValidationDate", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE })
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidatedDocumentsDTO.class)))
	@Operation(summary = "Modifica data di validazione", description = "Modifica la data di validazione di un validatedDocument.")
	@SecurityRequirements({
		@SecurityRequirement(name = "bearerAuth"),
		@SecurityRequirement(name = "FSE-JWT-Signature")}) 
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Modifica eseguita", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)) })
	boolean updateValidationDate(@RequestBody(required = false) String worflowInworkflowInstanceId, int days, HttpServletRequest request);

}
