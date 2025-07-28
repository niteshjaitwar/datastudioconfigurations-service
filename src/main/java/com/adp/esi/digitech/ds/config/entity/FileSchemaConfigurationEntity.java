package com.adp.esi.digitech.ds.config.entity;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Entity representing MS FPS file schema configuration. Stores metadata,
 * original/selected schema JSON, and audit timestamps for MS FPS file
 * processing. This entity maps to the
 * IBPMADMIN.MS_FPS_FILE_SCHEMA_CONFIGURATION table.
 * 
 * @author rhidau
 */
@Entity
@Table(name = "MS_FPS_FILE_SCHEMA_CONFIGURATION")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSchemaConfigurationEntity {

	/**
	 * Primary key identifier for the MS FPS file schema configuration. Uses Oracle
	 * sequence IBPMADMIN.SEQ_MS_FPS_FILE_SCHEMA_CONFIGURATION.
	 */
	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MS_FPS_FILE_SCHEMA_CONFIGURATION")
	@SequenceGenerator(name = "SEQ_MS_FPS_FILE_SCHEMA_CONFIGURATION", sequenceName = "SEQ_MS_FPS_FILE_SCHEMA_CONFIGURATION", allocationSize = 1)
	private Long id;

	/**
	 * Business unit identifier for the schema configuration. Maps to BU column with
	 * VARCHAR2(50) constraint.
	 */
	@NotBlank(message = "Business unit cannot be blank")
	@Column(name = "BU")
	private String bu;

	/**
	 * Platform identifier where the schema will be applied. Maps to PLATFORM column
	 * with VARCHAR2(50) constraint.
	 */
	@NotBlank(message = "Platform cannot be blank")
	@Column(name = "PLATFORM")
	private String platform;

	/**
	 * Data category classification for the schema. Maps to DATA_CATEGORY column
	 * with VARCHAR2(100) constraint.
	 */
	@NotBlank(message = "Data category cannot be blank")
	@Column(name = "DATA_CATEGORY")
	private String dataCategory;

	/**
	 * Unique source key identifier for the schema configuration. Maps to SOURCE_KEY
	 * column with VARCHAR2(100) constraint.
	 */
	@NotBlank(message = "Source key cannot be blank")
	@Column(name = "SOURCE_KEY")
	private String sourceKey;

	/**
	 * Original file content as CLOB. Maps to ORIGINAL_FILE column for storing raw
	 * file content.
	 */
	@Lob
	@Column(name = "ORIGINAL_FILE")
	private String originalFile;

	/**
	 * Original file schema in JSON format as CLOB. Maps to ORIGINAL_FILE_JSON
	 * column for storing parsed schema structure.
	 */
	@Lob
	@Column(name = "ORIGINAL_FILE_JSON")
	private String originalFileJson;

	/**
	 * User-selected schema elements in JSON format as CLOB. Maps to
	 * SELECTED_FILE_JSON column for storing filtered schema.
	 */
	@Lob
	@Column(name = "SELECTED_FILE_JSON")
	private String selectedFileJson;

	/**
	 * Generated template content as CLOB. Maps to TEMPLATE column for storing
	 * generated templates.
	 */
	@Lob
	@Column(name = "TEMPLATE")
	private String template;

	/**
	 * Schema version identifier. Maps to VERSION column with VARCHAR2(10)
	 * constraint.
	 */
	@Column(name = "VERSION")
	private String version;
	
	
	
	@Column(name = "PRIMARY_IDENTIFIER")
	private String primaryIdentifier;
	
	/**
	 * User identifier who created the schema configuration. Maps to CREATED_BY
	 * column with VARCHAR2(100) constraint.
	 */
	@Column(name = "CREATED_BY")
	private String createdBy;

	/**
	 * Timestamp when the schema configuration was created. Maps to
	 * CREATED_DATE_TIME column with TIMESTAMP(6) precision.
	 */
	@CreationTimestamp
	@Column(name = "CREATED_DATE_TIME", updatable = false)
	private Timestamp createdDateTime;

	/**
	 * User identifier who last updated the schema configuration. Maps to
	 * MODIFIED_BY column with VARCHAR2(100) constraint.
	 */
	@Column(name = "MODIFIED_BY")
	private String modifiedBy;

	/**
	 * Timestamp when the schema configuration was last updated. Maps to
	 * MODIFIED_DATE_TIME column with TIMESTAMP(6) precision.
	 */
	@UpdateTimestamp
	@Column(name = "MODIFIED_DATE_TIME")
	private Timestamp modifiedDateTime;
	
	@Column(name="USERROLE")
	private String userrole;
}
