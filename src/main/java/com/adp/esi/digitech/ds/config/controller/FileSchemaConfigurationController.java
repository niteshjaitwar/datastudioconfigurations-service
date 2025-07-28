package com.adp.esi.digitech.ds.config.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.file.schema.service.XSDSchemaConfigurationService;
import com.adp.esi.digitech.ds.config.model.ApiResponse;
import com.adp.esi.digitech.ds.config.model.ErrorResponse;
import com.adp.esi.digitech.ds.config.model.FileSchemaConfiguration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for managing MS FPS file schema configurations. Handles upload
 * (with upsert), retrieval, selection, and deletion operations. Provides safe
 * and meaningful error responses for all scenarios. All operations require bu,
 * platform, dataCategory, and sourceKey parameters.
 * 
 * @author rhidau
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/ahub/ds/config/file/schema")
@CrossOrigin(origins = "${app.allowed-origins}")
@Tag(name = "MS FPS File Schema Configuration", description = "Manage MS FPS file schema configurations")
public class FileSchemaConfigurationController {

	@Autowired
	private XSDSchemaConfigurationService schemaService;

	/**
	 * Uploads or updates an MS FPS schema file and stores the parsed JSON. If an
	 * entry with the given parameters exists, updates it and resets
	 * selectedFileJson.
	 * 
	 * @param bu           Business unit name (required)
	 * @param platform     Platform name (required)
	 * @param dataCategory Data category (required)
	 * @param sourceKey    Unique source key (required)
	 * @param file         MS FPS file to upload (required)
	 * @param useremail    User email who created the configuration (required)
	 * @param userrole     User role who created the configuration (required)
	 * @return Uploaded or updated schema configuration in a standardized response
	 */
	@PostMapping("/xsd/upload")
	@Operation(summary = "Upload MS FPS schema file", description = "Uploads and parses MS FPS schema file with upsert functionality")
	public ResponseEntity<ApiResponse<FileSchemaConfiguration>> uploadXSDSchema(
			@RequestParam @NotBlank @Parameter(description = "Business unit name") String bu,
			@RequestParam @NotBlank @Parameter(description = "Platform name") String platform,
			@RequestParam @NotBlank @Parameter(description = "Data category") String dataCategory,
			@RequestParam @NotBlank @Parameter(description = "Unique source key") String sourceKey,
			@RequestParam("file") @Parameter(description = "XSD file to upload") MultipartFile file,
			@RequestParam @NotBlank @Parameter(description = "Useremail who created the configuration") String useremail,
			@RequestParam @NotBlank @Parameter(description = "Userrole who created the configuration") String userrole) {

		log.info(
				"FileSchemaConfigurationController - uploadXSDSchema() - Uploading MS FPS schema for sourceKey={}, bu={}, platform={}, dataCategory={}",
				sourceKey, bu, platform, dataCategory);

		try {
			var result = schemaService.processSchema(bu, platform, dataCategory, sourceKey, file, useremail, userrole);
			log.info(
					"FileSchemaConfigurationController - uploadXSDSchema() - Successfully uploaded MS FPS schema for sourceKey={}",
					sourceKey);
			return ResponseEntity.ok(ApiResponse.success(ApiResponse.Status.SUCCESS, result));

		} catch (ConfigurationException ce) {
			log.error("FileSchemaConfigurationController - uploadXSDSchema() - Upload failed: {}", ce.getMessage());
			return ResponseEntity.unprocessableEntity()
					.body(ApiResponse.error(ApiResponse.Status.ERROR, new ErrorResponse("422", ce.getMessage())));
		} catch (DataAccessException dae) {
			log.error("FileSchemaConfigurationController - uploadXSDSchema() - Database error: {}", dae.getMessage(),
					dae);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(
					ApiResponse.Status.ERROR,
					new ErrorResponse("500", "Database operation failed. Please ensure required tables exist.")));
		} catch (Exception ex) {
			log.error("FileSchemaConfigurationController - uploadXSDSchema() - Unexpected error: {}", ex.getMessage(),
					ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ApiResponse.Status.ERROR,
							new ErrorResponse("500", "Unexpected system error. Please try again later.")));
		}
	}
	
	@PostMapping
	public ResponseEntity<ApiResponse<FileSchemaConfiguration>> addFileSchemaConfiguration(@Valid @RequestBody FileSchemaConfiguration fileSchemaConfiguration) {
		log.info("FileSchemaConfigurationController - addFileSchemaConfiguration() Started adding file schema config Data, fileSchemaConfiguration = {}", fileSchemaConfiguration.getBu());
		ApiResponse<FileSchemaConfiguration> response = null;

		try {
			var data = schemaService.saveEntity(fileSchemaConfiguration);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("FileSchemaConfigurationController - addFileSchemaConfiguration() Completed adding file schema config Data, fileSchemaConfiguration = {}",data.getBu());
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("FileSchemaConfigurationController - addFileSchemaConfiguration() Failed to adding file schema config Data for fileSchemaConfiguration = {}, Exception Message : {} ", fileSchemaConfiguration.getBu(), e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PatchMapping
	public ResponseEntity<ApiResponse<List<FileSchemaConfiguration>>> partialUpdate(@RequestBody List<Map<String, String>> fileSchemaConfigurations) {
		log.info("FileSchemaConfigurationController - partialUpdate()  Started Partial Updating Data");
		ApiResponse<List<FileSchemaConfiguration>> response = null;
		try {
			var data = schemaService.patch(fileSchemaConfigurations);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("FileSchemaConfigurationController - partialUpdate()  Completed Partial Updating Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("FileSchemaConfigurationController - partialUpdate()  Failed to Partial Update Data, Exception Message : {} ", e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}

	/**
	 * Returns all available schema configurations.
	 * 
	 * @return List of schema configurations
	 */
	@GetMapping
	@Operation(summary = "Get all schemas", description = "Retrieves all MS FPS schema configurations")
	public ResponseEntity<ApiResponse<List<FileSchemaConfiguration>>> getAllSchemas() {
		log.info("FileSchemaConfigurationController - getAllSchemas() - Retrieving all MS FPS schema configurations");
		var schemas = schemaService.findAll();
		log.info("FileSchemaConfigurationController - getAllSchemas() - Retrieved {} MS FPS schema configurations",
				schemas.size());
		return ResponseEntity.ok(ApiResponse.success(ApiResponse.Status.SUCCESS, schemas));
	}
	
	
	/**
	 * Fetches a schema configuration by all four key parameters.
	 * 
	 * @param bu           Business unit
	 * @param platform     Platform name
	 * @param dataCategory Data category
	 * @param sourceKey    Source key
	 * @param version	   Version
	 * @return Schema configuration if found, or error response
	 */
	
	
	@PostMapping("/by")
	@Operation(summary = "Get schema by all parameters", description = "Retrieves MS FPS schema configuration by all key parameters")
	public ResponseEntity<ApiResponse<List<FileSchemaConfiguration>>> getSchemaByAllParameters(
			@RequestParam @NotBlank @Parameter(description = "Business unit") String bu,
			@RequestParam @NotBlank @Parameter(description = "Platform name") String platform,
			@RequestParam @NotBlank @Parameter(description = "Data category") String dataCategory,
			@RequestParam @Parameter(description = "Source key", required = false) String sourceKey,
			@RequestParam @Parameter(description = "version", required =  false) String version) {
		
		log.info(
				"FileSchemaConfigurationController - getSchemaByAllParameters() - Retrieving MS FPS schema configuration for bu={}, platform={}, dataCategory={}, sourceKey={}, version={}",
				bu, platform, dataCategory, sourceKey, version);

		try {
			var schemas = schemaService.findBy(bu, platform, dataCategory, sourceKey, version);
			if (schemas == null || schemas.isEmpty()) {
				log.warn(
						"FileSchemaConfigurationController - getSchemaByAllParameters() - No schemas found for the provided parameters");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ApiResponse.Status.ERROR,
						new ErrorResponse("404", "Schemas not found for the provided parameters")));
			}
			log.info(
					"FileSchemaConfigurationController - getSchemaByAllParameters() - Found {} schemas for the provided parameters",
					schemas.size());
			return ResponseEntity.ok(ApiResponse.success(ApiResponse.Status.SUCCESS, schemas));
		} catch (ConfigurationException ce) {
			log.error("FileSchemaConfigurationController - getSchemaByAllParameters() - Find by parameters failed: {}",
					ce.getMessage());
			return ResponseEntity.unprocessableEntity()
					.body(ApiResponse.error(ApiResponse.Status.ERROR, new ErrorResponse("422", ce.getMessage())));
		}
	
	}

	/**
	 * Fetches a single schema by ID.
	 * 
	 * @param id Schema configuration ID
	 * @return Schema configuration if found, or error response
	 */
	@GetMapping("/{id}")
	@Operation(summary = "Get schema by ID", description = "Retrieves a specific MS FPS schema configuration by ID")
	public ResponseEntity<ApiResponse<FileSchemaConfiguration>> getSchemaById(
			@PathVariable @Parameter(description = "Schema configuration ID") Long id) {

		log.info(
				"FileSchemaConfigurationController - getSchemaById() - Retrieving MS FPS schema configuration by ID: {}",
				id);
		try {
			var schema = schemaService.findById(id);
			log.info("FileSchemaConfigurationController - getSchemaById() - Successfully retrieved schema with ID: {}",
					id);
			return ResponseEntity.ok(ApiResponse.success(ApiResponse.Status.SUCCESS, schema));
		} catch (ConfigurationException ce) {
			log.error("FileSchemaConfigurationController - getSchemaById() - Schema not found for ID: {}", id);
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.error(ApiResponse.Status.ERROR, new ErrorResponse("404", "Schema not found")));
		}
	}

	/**
	 * Deletes a schema configuration by ID.
	 * 
	 * @param id Schema configuration ID
	 * @return Success message or error response
	 */
	@DeleteMapping("/{id}")
	@Operation(summary = "Delete schema", description = "Deletes a MS FPS schema configuration by ID")
	public ResponseEntity<ApiResponse<String>> deleteSchema(
			@PathVariable @Parameter(description = "Schema configuration ID") Long id) {

		log.info(
				"FileSchemaConfigurationController - deleteSchema() - Deleting MS FPS schema configuration with ID: {}",
				id);

		try {
			schemaService.deleteSingle(id);
			log.info("FileSchemaConfigurationController - deleteSchema() - Successfully deleted schema with ID: {}",
					id);
			return ResponseEntity.ok(ApiResponse.success(ApiResponse.Status.SUCCESS, "Schema deleted successfully"));

		} catch (ConfigurationException ce) {
			log.error("FileSchemaConfigurationController - deleteSchema() - Delete failed: {}", ce.getMessage());
			return ResponseEntity.unprocessableEntity()
					.body(ApiResponse.error(ApiResponse.Status.ERROR, new ErrorResponse("422", ce.getMessage())));
		} catch (Exception ex) {
			log.error("FileSchemaConfigurationController - deleteSchema() - Unexpected error during delete: {}",
					ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ApiResponse.Status.ERROR,
							new ErrorResponse("500", "Unexpected error occurred while deleting schema.")));
		}
	}

	/**
	 * Deletes a schema configuration by all four key parameters.
	 * 
	 * @param bu           Business unit
	 * @param platform     Platform name
	 * @param dataCategory Data category
	 * @param sourceKey    Source key
	 * @return Success message or error response
	 */
	@DeleteMapping("/by")
	@Operation(summary = "Delete schema by parameters", description = "Deletes MS FPS schema configuration by all key parameters")
	public ResponseEntity<ApiResponse<String>> deleteSchemaByParameters(
			@RequestParam @NotBlank @Parameter(description = "Business unit") String bu,
			@RequestParam @NotBlank @Parameter(description = "Platform name") String platform,
			@RequestParam @NotBlank @Parameter(description = "Data category") String dataCategory,
			@RequestParam @Parameter(description = "Source key", required =  false) String sourceKey,
			@RequestParam @Parameter(description = "version", required =  false) String version) {

		log.info(
				"FileSchemaConfigurationController - deleteSchemaByParameters() - Deleting MS FPS schema configuration for bu={}, platform={}, dataCategory={}, sourceKey={}, version={}",
				bu, platform, dataCategory, sourceKey, version);

		try {
			schemaService.deleteBy(bu, platform, dataCategory, sourceKey, version);
			log.info(
					"FileSchemaConfigurationController - deleteSchemaByParameters() - Successfully deleted schema for provided parameters");
			return ResponseEntity.ok(ApiResponse.success(ApiResponse.Status.SUCCESS, "Schema deleted successfully"));

		} catch (ConfigurationException ce) {
			log.error(
					"FileSchemaConfigurationController - deleteSchemaByParameters() - Delete by parameters failed: {}",
					ce.getMessage());
			return ResponseEntity.unprocessableEntity()
					.body(ApiResponse.error(ApiResponse.Status.ERROR, new ErrorResponse("422", ce.getMessage())));
		} catch (Exception ex) {
			log.error(
					"FileSchemaConfigurationController - deleteSchemaByParameters() - Unexpected error during delete: {}",
					ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ApiResponse.Status.ERROR,
							new ErrorResponse("500", "Unexpected error occurred while deleting schema.")));
		}
	}

	/**
	 * Deletes multiple schema configurations by IDs.
	 * 
	 * @param ids List of schema configuration IDs
	 * @return Success message or error response
	 */
	@DeleteMapping("/bulk")
	@Operation(summary = "Bulk delete schemas", description = "Deletes multiple MS FPS schema configurations by IDs")
	public ResponseEntity<ApiResponse<String>> bulkDeleteSchemas(
			@RequestBody @Parameter(description = "List of schema configuration IDs") List<Long> ids) {

		log.info(
				"FileSchemaConfigurationController - bulkDeleteSchemas() - Bulk deleting MS FPS schema configurations with IDs: {}",
				ids);

		try {
			if (ids == null || ids.isEmpty()) {
				log.warn("FileSchemaConfigurationController - bulkDeleteSchemas() - IDs list is null or empty");
				return ResponseEntity.unprocessableEntity().body(ApiResponse.error(ApiResponse.Status.ERROR,
						new ErrorResponse("422", "IDs list cannot be null or empty")));
			}

			schemaService.deleteBulk(ids);
			log.info(
					"FileSchemaConfigurationController - bulkDeleteSchemas() - Successfully bulk deleted {} schema configurations",
					ids.size());
			return ResponseEntity.ok(ApiResponse.success(ApiResponse.Status.SUCCESS,
					"Successfully deleted " + ids.size() + " schema configurations"));

		} catch (ConfigurationException ce) {
			log.error("FileSchemaConfigurationController - bulkDeleteSchemas() - Bulk delete failed: {}",
					ce.getMessage());
			return ResponseEntity.unprocessableEntity()
					.body(ApiResponse.error(ApiResponse.Status.ERROR, new ErrorResponse("422", ce.getMessage())));
		} catch (Exception ex) {
			log.error(
					"FileSchemaConfigurationController - bulkDeleteSchemas() - Unexpected error during bulk delete: {}",
					ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ApiResponse.Status.ERROR,
							new ErrorResponse("500", "Unexpected error occurred while deleting schemas.")));
		}
	}
	
	
	/**
	 * Generates column relations from selected XSD elements.
	 * 
	 * @param bu           Business unit name (required)
	 * @param platform     Platform name (required)
	 * @param dataCategory Data category (required)
	 * @param sourceKey    Unique source key (required)
	 * @param userEmail    User email for audit (required)
	 * @param userRole     User role for audit (required)
	 * @return Generated column configuration response
	 */
	
	@PostMapping("/xsd/generate/template")
	@Operation(summary = "Generate column relations", description = "Generates column relations from selected XSD elements")
	public ResponseEntity<ApiResponse<FileSchemaConfiguration>> generateColumnRelations(
			@RequestParam @NotBlank @Parameter(description = "Business unit name") String bu,
			@RequestParam @NotBlank @Parameter(description = "Platform name") String platform,
			@RequestParam @NotBlank @Parameter(description = "Data category") String dataCategory,
			@RequestParam @NotBlank @Parameter(description = "Unique source key") String sourceKey,
			@RequestParam @NotBlank @Parameter(description = "User email for audit") String userEmail,
			@RequestParam @NotBlank @Parameter(description = "User role for audit") String userRole) {

		log.info("Generating column relations for sourceKey={}, bu={}, platform={}, dataCategory={}", sourceKey, bu,
				platform, dataCategory);

		try {
			var result = schemaService.generateTemplate(bu, platform, dataCategory, sourceKey, userEmail,
					userRole);
			return ResponseEntity.ok(ApiResponse.success(ApiResponse.Status.SUCCESS, result));

		} catch (ConfigurationException ce) {
			log.error("Generate column relations failed: {}", ce.getMessage());
			return ResponseEntity.unprocessableEntity()
					.body(ApiResponse.error(ApiResponse.Status.ERROR, new ErrorResponse("422", ce.getMessage())));
		} catch (Exception ex) {
			log.error("Unexpected error: {}", ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ApiResponse.Status.ERROR,
							new ErrorResponse("500", "Unexpected error occurred while generating column relations.")));
		}
	}
	
	
	/**
	 * Searches for schema configurations by business parameters.
	 * 
	 * @param bu           Business unit
	 * @param platform     Platform name
	 * @param dataCategory Data category
	 * @return List of matching schema configurations
	 */
	/*
	@GetMapping("/schemas/search")
	@Operation(summary = "Search schemas", description = "Searches MS FPS schema configurations by business parameters")
	public ResponseEntity<ApiResponse<List<FileSchemaConfiguration>>> searchSchemas(
			@RequestParam @NotBlank @Parameter(description = "Business unit") String bu,
			@RequestParam @NotBlank @Parameter(description = "Platform name") String platform,
			@RequestParam @NotBlank @Parameter(description = "Data category") String dataCategory) {

		log.info("Searching MS FPS schemas for bu={}, platform={}, dataCategory={}", bu, platform, dataCategory);

		try {
			var schemas = schemaService.findByBuPlatformAndDataCategory(bu, platform, dataCategory);
			return ResponseEntity.ok(ApiResponse.success(ApiResponse.Status.SUCCESS, schemas));
		} catch (ConfigurationException ce) {
			log.error("Search failed: {}", ce.getMessage());
			return ResponseEntity.unprocessableEntity()
					.body(ApiResponse.error(ApiResponse.Status.ERROR, new ErrorResponse("422", ce.getMessage())));
		}
	}
	*/

	/**
	 * Fetches a schema configuration by all four key parameters.
	 * 
	 * @param bu           Business unit
	 * @param platform     Platform name
	 * @param dataCategory Data category
	 * @param sourceKey    Source key
	 * @return Schema configuration if found, or error response
	 */
	/*
	@GetMapping("/by")
	@Operation(summary = "Get schema by all parameters", description = "Retrieves MS FPS schema configuration by all key parameters")
	public ResponseEntity<ApiResponse<FileSchemaConfiguration>> getSchemaByAllParameters(
			@RequestParam @NotBlank @Parameter(description = "Business unit") String bu,
			@RequestParam @NotBlank @Parameter(description = "Platform name") String platform,
			@RequestParam @NotBlank @Parameter(description = "Data category") String dataCategory,
			@RequestParam @NotBlank @Parameter(description = "Source key") String sourceKey) {

		log.info("Retrieving MS FPS schema configuration for bu={}, platform={}, dataCategory={}, sourceKey={}", bu,
				platform, dataCategory, sourceKey);

		try {
			var schema = schemaService.findByBuPlatformDataCategoryAndSourceKey(bu, platform, dataCategory, sourceKey);
			if (schema == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ApiResponse.Status.ERROR,
						new ErrorResponse("404", "Schema not found for the provided parameters")));
			}
			return ResponseEntity.ok(ApiResponse.success(ApiResponse.Status.SUCCESS, schema));
		} catch (ConfigurationException ce) {
			log.error("Find by parameters failed: {}", ce.getMessage());
			return ResponseEntity.unprocessableEntity()
					.body(ApiResponse.error(ApiResponse.Status.ERROR, new ErrorResponse("422", ce.getMessage())));
		}
	}
	*/
	
	/**
	 * Saves a user-selected tree structure for an existing schema.
	 * 
	 * @param bu               Business unit name (required)
	 * @param platform         Platform name (required)
	 * @param dataCategory     Data category (required)
	 * @param sourceKey        Unique source key (required)
	 * @param selectedFileJson JSON string representing the selected tree (required)
	 * @param modifiedBy       User who modified the configuration (required)
	 * @return Updated schema configuration in a standardized response
	 */
	
	/*
	@PatchMapping("/xsd")
	@Operation(summary = "Save selected tree", description = "Saves user-selected schema elements for an existing configuration")
	public ResponseEntity<ApiResponse<FileSchemaConfiguration>> saveSelectedXSDTree(
			@RequestParam @NotBlank @Parameter(description = "Business unit name") String bu,
			@RequestParam @NotBlank @Parameter(description = "Platform name") String platform,
			@RequestParam @NotBlank @Parameter(description = "Data category") String dataCategory,
			@RequestParam @NotBlank @Parameter(description = "Unique source key") String sourceKey,
			@RequestBody @Parameter(description = "Selected schema elements in JSON format") String selectedFileJson,
			@RequestParam @NotBlank @Parameter(description = "User who modified the configuration") String modifiedBy) {

		log.info("Saving selected tree for sourceKey={}, bu={}, platform={}, dataCategory={}", sourceKey, bu, platform,
				dataCategory);

		try {
			if (selectedFileJson == null || selectedFileJson.trim().isEmpty()) {
				return ResponseEntity.unprocessableEntity().body(ApiResponse.error(ApiResponse.Status.ERROR,
						new ErrorResponse("422", "Selected file JSON must not be empty.")));
			}

			var result = schemaService.saveSelectedTree(bu, platform, dataCategory, sourceKey, selectedFileJson,
					modifiedBy);
			return ResponseEntity.ok(ApiResponse.success(ApiResponse.Status.SUCCESS, result));

		} catch (ConfigurationException ce) {
			log.error("Save selected tree failed: {}", ce.getMessage());
			return ResponseEntity.unprocessableEntity()
					.body(ApiResponse.error(ApiResponse.Status.ERROR, new ErrorResponse("422", ce.getMessage())));
		} catch (Exception ex) {
			log.error("Unexpected error: {}", ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ApiResponse.Status.ERROR,
							new ErrorResponse("500", "Unexpected error occurred while saving selected schema.")));
		}
	}
	*/
}
