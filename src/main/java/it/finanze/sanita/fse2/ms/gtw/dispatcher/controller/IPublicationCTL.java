package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.PublicationCreationResDTO;

/**
 * 
 * @author CPIERASC
 *
 *	Controller publication.
 */
@RequestMapping(path = "/v1")
@Tag(name = "Servizio pubblicazione documenti")
public interface IPublicationCTL {
 
	@PostMapping(value = "/publish-creation", consumes = {"multipart/form-data"})
	@Operation(summary = "Pubblicazione creazione documenti", description = "Pubblica con l'intento di generare nuove risorse FHIR.",
				security = @SecurityRequirement(name = "bearerAuth" ))
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PublicationCreationResDTO.class)))
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Validazione eseguita senza inserimento in cache", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PublicationCreationResDTO.class))),
			@ApiResponse(responseCode = "201", description = "Presa in carico eseguita con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PublicationCreationResDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "401", description = "Token jwt mancante", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	PublicationCreationResDTO publicationCreation(@RequestBody(required=false) PublicationCreationReqDTO requestBody, @RequestPart("file") MultipartFile file , HttpServletRequest request);

}
