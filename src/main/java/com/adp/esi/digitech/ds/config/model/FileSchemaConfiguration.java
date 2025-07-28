package com.adp.esi.digitech.ds.config.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for MS FPS file schema configuration. Used for API requests and responses
 * in MS FPS file processing workflows. Maps to
 * IBPMADMIN.MS_FPS_FILE_SCHEMA_CONFIGURATION table structure.
 * 
 * @author rhidau
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSchemaConfiguration {

	/**
	 * Unique identifier for the MS FPS file schema configuration. Corresponds to ID
	 * column in the database.
	 */
	private Long id;

	/**
	 * Business unit name for organizational segregation. Corresponds to BU column
	 * (VARCHAR2(50)).
	 */
	@NotBlank(message = "Business unit must not be blank")
	private String bu;

	/**
	 * Platform name where the schema will be applied. Corresponds to PLATFORM
	 * column (VARCHAR2(50)).
	 */
	@NotBlank(message = "Platform must not be blank")
	private String platform;

	/**
	 * Data category for schema classification. Corresponds to DATA_CATEGORY column
	 * (VARCHAR2(100)).
	 */
	@NotBlank(message = "Data category must not be blank")
	private String dataCategory;

	/**
	 * Unique source key for the schema configuration. Corresponds to SOURCE_KEY
	 * column (VARCHAR2(100)).
	 */
	@NotBlank(message = "Source key must not be blank")
	private String sourceKey;

	/**
	 * Original file content. Corresponds to ORIGINAL_FILE column (CLOB).
	 */
	private String originalFile;

	/**
	 * Original file schema in JSON format. Corresponds to ORIGINAL_FILE_JSON column
	 * (CLOB).
	 */
	private String originalFileJson;

	/**
	 * User-selected schema elements in JSON format. Corresponds to
	 * SELECTED_FILE_JSON column (CLOB).
	 */
	private String selectedFileJson;

	/**
	 * Generated template content. Corresponds to TEMPLATE column (CLOB).
	 */
	private String template;

	/**
	 * Schema version identifier. Corresponds to VERSION column (VARCHAR2(10)).
	 */
	private String version;
	
	private String primaryIdentifier;
	
	private String useremail;
	
	private String userrole;
}
