/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;

@Configuration 
@SecuritySchemes( {
	@SecurityScheme(
			name = "bearerAuth", 
			type = SecuritySchemeType.HTTP, 
			bearerFormat = "JWT", 
			scheme = "bearer", 
			description = "JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token} [RFC8725](https://tools.ietf.org/html/RFC8725).\""),
	@SecurityScheme(
			name = "FSE-JWT-Signature", 
			type = SecuritySchemeType.APIKEY,
			in = SecuritySchemeIn.HEADER)
    }
)
public class OpenApiCFG {

	@Autowired
	private CustomSwaggerCFG customOpenapi;

	public OpenApiCFG() {
		// Empty constructor.
	}
	
	@Bean
	public OpenApiCustomiser openApiCustomiser() {

		final List<String> required = new ArrayList<>();
		required.add("file");
		required.add("requestBody");

		return openApi -> {

			// Populating info section.
			openApi.getInfo().setTitle(customOpenapi.getTitle());
			openApi.getInfo().setVersion(customOpenapi.getVersion());
			openApi.getInfo().setDescription(customOpenapi.getDescription());
			openApi.getInfo().setTermsOfService(customOpenapi.getTermsOfService());

			// Adding contact to info section
			final Contact contact = new Contact();
			contact.setName(customOpenapi.getContactName());
			contact.setUrl(customOpenapi.getContactUrl());
			contact.setEmail(customOpenapi.getContactMail());
			openApi.getInfo().setContact(contact);

			// Adding extensions
			openApi.getInfo().addExtension("x-api-id", customOpenapi.getApiId());
			openApi.getInfo().addExtension("x-summary", customOpenapi.getApiSummary());

			for (final Server server : openApi.getServers()) {
				final Pattern pattern = Pattern.compile("^https://.*");
				if (!pattern.matcher(server.getUrl()).matches()) {
					server.addExtension("x-sandbox", true);
				}
			}

			openApi.getComponents().getSchemas().values().forEach(schema -> {
				schema.setAdditionalProperties(false);
			});

			openApi.getPaths().values()
			.stream()
			.map(item -> getFileSchema(item))
			.filter(Objects::nonNull)
			.forEach(schema -> {
				schema.additionalProperties(false);
				schema.getProperties().get("file").setMaxLength(customOpenapi.getFileMaxLength());
				schema.required(required);
			});
		};
	}

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
				.addSecurityItem(new SecurityRequirement().addList("FSE-JWT-Signature"));
	}

	@Bean
	public OpenApiCustomiser customerGlobalHeaderOpenApiCustomiser() {
		return openApi -> {
			openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
				final ApiResponses apiResponses = operation.getResponses();

				final Schema<Object> errorResponseSchema = new Schema<>();
				errorResponseSchema.setName("Error");
				errorResponseSchema.set$ref("#/components/schemas/ErrorResponseDTO");
				final MediaType media = new MediaType();
				media.schema(errorResponseSchema);
				final ApiResponse apiResponse = new ApiResponse().description("default")
						.content(new Content()
								.addMediaType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE,
										media));
				apiResponses.addApiResponse("default", apiResponse);
			}));
		};
	}

	private Schema<?> getFileSchema(PathItem item) {
		MediaType mediaType = getMultipartFile(item);
		if (mediaType == null) return null;
		return mediaType.getSchema();
	}
	
	private Operation getOperation(PathItem item) {
		if (item.getPost() != null) return item.getPost();
		if (item.getPatch() != null) return item.getPatch();
		if (item.getPut() != null) return item.getPut();
		return null;
	}
	
	private MediaType getMultipartFile(PathItem item) {
		Operation operation = getOperation(item);
		if (operation == null) return null;
		RequestBody body = operation.getRequestBody();
		if (body == null) return null;
		Content content = body.getContent();
		if (content == null) return null;
		MediaType mediaType = content.get(org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE);
		return mediaType;
	}
}
