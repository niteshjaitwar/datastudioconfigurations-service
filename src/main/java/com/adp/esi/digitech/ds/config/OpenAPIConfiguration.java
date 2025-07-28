package com.adp.esi.digitech.ds.config;

import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import com.adp.esi.digitech.ds.config.model.ErrorData;
import com.adp.esi.digitech.ds.config.model.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;

@Configuration
public class OpenAPIConfiguration {

	private static final String DATA_TYPE = "string";
	
	@Value("${contact.team.name}")
	String contactName;
	
	@Value("${contact.team.mail}")
	String contactMail;
	
	@Value("${spring.application.version}")
	String version;
	
	@Value("${spring.application.title}")
	String title;
	
	@Value("${spring.application.description}")
	String description;
	
	

    @Bean
	public OpenApiCustomizer globalResponseCustomizer() throws JsonProcessingException {

		var myContact = new Contact().name(contactName).email(contactMail);

		var information = new Info().title(title).version(version)
				.description(description).contact(myContact);

		var badRequestApiResponse = getApiResponse(HttpStatus.BAD_REQUEST);
		var internalserverErrorApiResponse = getApiResponse(HttpStatus.INTERNAL_SERVER_ERROR);
		var noContentApiResponse = getApiResponse(HttpStatus.NO_CONTENT);
		
		return openApi -> {
			openApi.info(information).getPaths().values()
					.forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
						operation.getResponses().put(getCode(HttpStatus.BAD_REQUEST), badRequestApiResponse);
						operation.getResponses().put(getCode(HttpStatus.INTERNAL_SERVER_ERROR), internalserverErrorApiResponse);
						operation.getResponses().put(getCode(HttpStatus.NO_CONTENT), noContentApiResponse);
					}));
		};
	}	
    
	private ApiResponse getApiResponse(HttpStatus status) throws JsonProcessingException {
		var errorData = List.of(new ErrorData(DATA_TYPE, DATA_TYPE));
		var error = com.adp.esi.digitech.ds.config.model.ApiResponse
				.error(com.adp.esi.digitech.ds.config.model.ApiResponse.Status.ERROR, new ErrorResponse(getCode(status), DATA_TYPE, errorData));
		return new ApiResponse().content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
				new MediaType().schema(new Schema<ApiResponse>().example(error))))
				.description(status.getReasonPhrase());
	}
	
	private String getCode(HttpStatus status) {
		return String.valueOf(status.value());
	}

}
