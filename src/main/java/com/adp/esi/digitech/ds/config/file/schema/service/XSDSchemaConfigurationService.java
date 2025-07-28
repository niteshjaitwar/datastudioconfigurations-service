package com.adp.esi.digitech.ds.config.file.schema.service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.adp.esi.digitech.ds.config.entity.FileSchemaConfigurationEntity;
import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.model.ColumnConfiguration;
import com.adp.esi.digitech.ds.config.model.ColumnConfigurationResponse;
import com.adp.esi.digitech.ds.config.model.ColumnRelation;
import com.adp.esi.digitech.ds.config.model.ErrorData;
import com.adp.esi.digitech.ds.config.model.FileSchemaConfiguration;
import com.adp.esi.digitech.ds.config.model.XSDNode;
import com.adp.esi.digitech.ds.config.util.ValidationUtil;
import com.adp.esi.digitech.ds.enums.DataType;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing MS FPS file schema configurations. Handles upload,
 * parsing, selection, retrieval, and deletion operations for MS FPS file
 * schemas.
 *
 * This service extends AbstractFileSchemaConfigurationService to leverage
 * common configuration management patterns and provides specialized operations
 * for MS FPS file schema processing workflows, including integration with
 * ColumnConfigurationService for generating column relations from selected XSD
 * elements.
 *
 * @author rhidau
 */
@Service
@Slf4j
public class XSDSchemaConfigurationService extends AbstractFileSchemaConfigurationService {

	@Autowired
	private XSDParseService xsdParseService;

	@Value("#{'${datastudio.config.skip.tags}'.split(',')}")
	private Set<String> METADATA_KEYS;

	/*
	 * private static final Set<String> METADATA_KEYS = Set.of("minOccurs",
	 * "maxOccurs", "minOccors", "maxOccors", "maxoccurs", "isAdded", "isUpdated",
	 * "isDeleted", "priorValue");
	 */
	/**
	 * Uploads and parses a new MS FPS schema file. Upserts by sourceKey. If an
	 * entry with sourceKey exists, updates originalFileJson and resets
	 * selectedFileJson. Otherwise, creates a new record.
	 *
	 * @param bu           Business unit name
	 * @param platform     Platform name
	 * @param dataCategory Data category
	 * @param sourceKey    Unique schema key
	 * @param file         Uploaded MS FPS file
	 * @param useremail    User email who created the configuration
	 * @param userrole     User role who created the configuration
	 * @return The saved MS FPS schema configuration
	 * @throws ConfigurationException if validation or parsing fails
	 */
	@Transactional
	@Override
	public FileSchemaConfiguration processSchema(String bu, String platform, String dataCategory, String sourceKey,
			MultipartFile file, String useremail, String userrole) {
		log.info("XSDSchemaConfigurationService - processSchema() - Processing MS FPS schema, sourceKey={}, bu={}, platform={}, dataCategory={}",
				sourceKey, bu, platform, dataCategory);

		// Validate parameters and file
		validateParametersAndFile(bu, platform, dataCategory, sourceKey, file, useremail, userrole);

		try {
			String originalFileContent = new String(file.getBytes(), StandardCharsets.UTF_8);
			XSDNode rootNode = xsdParseService.parseXsd(file);
			String originalFileJson = objectMapper.writeValueAsString(rootNode);

			var dto = FileSchemaConfiguration.builder()
											 .bu(bu)
											 .platform(platform)
											 .dataCategory(dataCategory)
											 .sourceKey(sourceKey)
											 .version(DEFAULT_VERSION_NAME)
											 .originalFile(originalFileContent)
											 .originalFileJson(originalFileJson)
											 .useremail(useremail)
											 .userrole(userrole)
											 .build();

			log.info("XSDSchemaConfigurationService - processSchema() - Successfully processed MS FPS schema for sourceKey={}",	sourceKey);
			return saveEntity(dto);
		} catch (IOException ex) {
			log.error("XSDSchemaConfigurationService - processSchema() - File reading failed for sourceKey={}: {}",
					sourceKey, ex.getMessage(), ex);
			throw new ConfigurationException("Failed to read MS FPS file: " + ex.getMessage(), ex);
		} catch (Exception ex) {
			log.error("XSDSchemaConfigurationService - processSchema() - Upload failed for sourceKey={}: {}", sourceKey,
					ex.getMessage(), ex);
			throw new ConfigurationException("MS FPS schema upload failed: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Updates a list of FileSchemaConfiguration entities with provided
	 * configuration data. Handles patching selectedFileJson and template
	 * generation, as well as auditing information. Now supports primaryIdentifier.
	 *
	 * @param configurations List of maps containing configuration fields to patch
	 * @return List of updated FileSchemaConfiguration DTOs
	 * @throws ConfigurationException if input validation fails or entities are not
	 *                                found
	 */
	@Override
	@Transactional
	public List<FileSchemaConfiguration> patch(List<Map<String, String>> configurations) {
		log.info("XSDSchemaConfigurationService - patch() - Started patching configurations METADATA_KEYS = {}",METADATA_KEYS);

		boolean isFound = configurations.parallelStream()
				.allMatch(rowMap -> rowMap.containsKey("id") && ValidationUtil.isHavingValue(rowMap.get("id")));
		if (!isFound)
			throw new ConfigurationException("id is mandatory and id should not be empty or null ");

		List<Long> ids = configurations.parallelStream().map(rowMap -> Long.valueOf(rowMap.get("id")))
				.collect(Collectors.toList());

		var originalEntities = fileSchemaConfigurationRepository.findAllById(ids);

		if (originalEntities == null || originalEntities.isEmpty())
			throw new ConfigurationException("No data found for given Id's" + ids);

		var modifiedEntities = originalEntities.stream().map(entity -> {
			Optional<Map<String, String>> optional = configurations.stream()
					.filter(validation -> entity.getId().compareTo(Long.valueOf(validation.get("id"))) == 0)
					.findFirst();
			if (optional.isPresent()) {
				var mapRow = optional.get();
				String primaryIdentifier = mapRow.getOrDefault("primaryIdentifier", entity.getPrimaryIdentifier());

				mapRow.forEach((key, value) -> {
					if (ValidationUtil.isHavingValue(key) && !key.equalsIgnoreCase("id")
							&& !key.equalsIgnoreCase("useremail")) {
						Field field = ReflectionUtils.findField(FileSchemaConfigurationEntity.class, key);
						if (field != null) {
							field.setAccessible(true);
							ReflectionUtils.setField(field, entity, value);
						} else {
							log.warn("XSDSchemaConfigurationService - patch() - Field {} not found in FileSchemaConfigurationEntity.", key);
						}
					}
					if (key.equalsIgnoreCase("useremail"))
						entity.setModifiedBy(value);

					// If selectedFileJson is updated, re-generate template with current
					// primaryIdentifier
					if (key.equalsIgnoreCase("selectedFileJson") && ValidationUtil.isHavingValue(value)) {
						log.info("XSDSchemaConfigurationService - patch() - Found selectedFileJson, generating column relations");
						var columnRelations = generateColumnRelations(entity.getBu(), entity.getPlatform(),
								entity.getDataCategory(), entity.getSourceKey(), entity.getCreatedBy(),
								entity.getUserrole(), value, primaryIdentifier);
						String templateJson = convertSelectedJsonToTemplate(value, columnRelations, primaryIdentifier);
						entity.setTemplate(templateJson);
					}

				});

				entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			}
			return entity;
		}).collect(Collectors.toList());

		var updatedEntities = fileSchemaConfigurationRepository.saveAll(modifiedEntities);
		log.info("XSDSchemaConfigurationService - patch() - Successfully patched {} configurations",
				updatedEntities.size());
		return updatedEntities.parallelStream().map(item -> {
			var temp = modelMapper.map(item, FileSchemaConfiguration.class);
			temp.setUseremail(
					ValidationUtil.isHavingValue(item.getModifiedBy()) ? item.getModifiedBy() : item.getCreatedBy());
			return temp;
		}).collect(Collectors.toList());
	}

	/**
	 * Generates a template from the selected schema elements and creates column
	 * relations using ColumnConfigurationService. This method converts selected XSD
	 * elements into ColumnConfiguration objects and calls the existing addbulk
	 * method to create column relations, validation rules, and transformation
	 * rules.
	 *
	 * @param bu           Business unit name
	 * @param platform     Platform name
	 * @param dataCategory Data category
	 * @param sourceKey    Unique schema key
	 * @param userEmail    User email for audit
	 * @param userRole     User role for audit
	 * @return Updated FileSchemaConfiguration with generated template
	 * @throws ConfigurationException if validation fails or processing errors occur
	 */
	@Transactional
	public FileSchemaConfiguration generateTemplate(String bu, String platform, String dataCategory, String sourceKey,
			String userEmail, String userRole) {

		log.info("XSDSchemaConfigurationService - generateTemplate() - Generating column relations for sourceKey={}, bu={}, platform={}, dataCategory={}",
				sourceKey, bu, platform, dataCategory);

		List<ErrorData> errors = new ArrayList<>();
		if (!ValidationUtil.isHavingValue(userEmail)) {
			errors.add(new ErrorData("VALIDATION_ERROR", "User email cannot be blank"));
		}
		if (!ValidationUtil.isHavingValue(userRole)) {
			errors.add(new ErrorData("VALIDATION_ERROR", "User role cannot be blank"));
		}
		if (!errors.isEmpty()) {
			ConfigurationException exception = new ConfigurationException("Validation failed");
			exception.setErrors(errors);
			throw exception;
		}

		try {
			FileSchemaConfigurationEntity entity = fileSchemaConfigurationRepository
					.findByBuAndPlatformAndDataCategoryAndSourceKeyAndVersion(bu, platform, dataCategory, sourceKey,
							DEFAULT_VERSION_NAME)
					.orElseThrow(() -> new ConfigurationException(
							String.format("Schema not found for bu=%s, platform=%s, dataCategory=%s, sourceKey=%s", bu,
									platform, dataCategory, sourceKey)));

			if (!ValidationUtil.isHavingValue(entity.getSelectedFileJson())) {
				throw new ConfigurationException(
						"No selected schema elements found. Please select schema elements first.");
			}

			// Generate template without primaryIdentifier for backward compatibility
			var columnRelations = generateColumnRelations(bu, platform, dataCategory, sourceKey, userEmail, userRole,
					entity.getSelectedFileJson(), entity.getPrimaryIdentifier());
			String templateJson = convertSelectedJsonToTemplate(entity.getSelectedFileJson(), columnRelations,
					entity.getPrimaryIdentifier());

			entity.setTemplate(templateJson);
			fileSchemaConfigurationRepository.save(entity);

			log.info("XSDSchemaConfigurationService - generateTemplate() - Successfully generated {} column relations for sourceKey={}",
					columnRelations.size(), sourceKey);

			return this.toDto(entity);

		} catch (Exception ex) {
			log.error("XSDSchemaConfigurationService - generateTemplate() - Failed to generate column relations for sourceKey={}: {}",
					sourceKey, ex.getMessage(), ex);
			throw new ConfigurationException("Failed to generate column relations: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Generates column relations from selected XSD elements using
	 * ColumnConfigurationService. Converts selected XSD elements into
	 * ColumnConfiguration objects and calls addbulk to create column relations,
	 * validation rules, and transformation rules.
	 *
	 * @param bu               Business unit name
	 * @param platform         Platform name
	 * @param dataCategory     Data category
	 * @param sourceKey        Unique schema key
	 * @param userEmail        User email for audit
	 * @param userRole         User role for audit
	 * @param selectedFileJson Filtered JSON representation of selected schema
	 *                         elements
	 * @return List<ColumnRelation> containing created relations and rules
	 * @throws ConfigurationException if schema not found or processing fails
	 */
	private List<ColumnRelation> generateColumnRelations(String bu, String platform, String dataCategory,
			String sourceKey, String userEmail, String userRole, String selectedFileJson, String primaryIdentifier) {

		try {
			XSDNode selectedSchema = objectMapper.readValue(selectedFileJson, XSDNode.class);
			List<ColumnConfiguration> columnConfigurations = convertXsdNodeToColumnConfigurations(selectedSchema, bu,
					platform, dataCategory, sourceKey, userEmail, userRole, primaryIdentifier);

			if (columnConfigurations.isEmpty()) {
				throw new ConfigurationException("No valid column configurations found in selected schema elements");
			}
			log.info("XSDSchemaConfigurationService - generateColumnRelations() - Generated {} column configurations for sourceKey={} with primaryIdentifier={}",
					columnConfigurations.size(), sourceKey, primaryIdentifier);

			//objectMapper.writeValue(new File("sample.json"), columnConfigurations);
			//return null;
			ColumnConfigurationResponse response = columnConfigurationService.addbulk(columnConfigurations);
			return response.getColumnRelations();

		} catch (Exception ex) {
			log.error("XSDSchemaConfigurationService - generateColumnRelations() - Failed to generate column relations for sourceKey={}: {}",
					sourceKey, ex.getMessage(), ex);
			throw new ConfigurationException("Failed to generate column relations: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Converts selectedFileJson to template format with UUID placeholders. This
	 * method generates a template structure suitable for runtime usage.
	 *
	 * @param selectedFileJson The filtered JSON representation of selected schema
	 *                         elements
	 * @param columnRelations  The list of column relations generated previously
	 * @return JSON string of the template structure with UUID placeholders
	 * @throws ConfigurationException if conversion fails
	 */
	private String convertSelectedJsonToTemplate(String selectedFileJson, List<ColumnRelation> columnRelations,
			String primaryIdentifier) {
		try {
			log.info("XSDSchemaConfigurationService - convertSelectedJsonToTemplate() - Converting selected JSON to template format with primaryIdentifier: {}",
					primaryIdentifier);

			XSDNode selectedSchema = objectMapper.readValue(selectedFileJson, XSDNode.class);

			Map<String, String> columnUuidMap = columnRelations.stream().collect(Collectors.toMap(
					ColumnRelation::getColumnName, ColumnRelation::getUuid, (existing, replacement) -> existing));

			Map<String, Object> template = new HashMap<>();

			// Add primaryIdentifier at the top level if provided
			if (ValidationUtil.isHavingValue(primaryIdentifier)) {
				template.put("primaryIdentifier", primaryIdentifier);
			}

			template.put("template", convertXsdNodeToTemplateNode(selectedSchema, columnUuidMap, ""));

			String templateJson = objectMapper.writeValueAsString(template);

			log.info("XSDSchemaConfigurationService - convertSelectedJsonToTemplate() - Successfully converted to template format with primaryIdentifier");
			return templateJson;

		} catch (Exception ex) {
			log.error("XSDSchemaConfigurationService - convertSelectedJsonToTemplate() - Failed to convert to template: {}",
					ex.getMessage());
			throw new ConfigurationException("Failed to convert selected JSON to template format: " + ex.getMessage(),
					ex);
		}
	}

	/**
	 * Recursively converts XSDNode to template node structure, excluding metadata
	 * nodes. Used for transforming schema nodes into a template structure with UUID
	 * placeholders.
	 *
	 * @param node          The current XSD node
	 * @param columnUuidMap Mapping from column names to UUIDs
	 * @param parentPath    Path for nested elements
	 * @return Map representing the template node structure
	 */
	private Map<String, Object> convertXsdNodeToTemplateNode(XSDNode node, Map<String, String> columnUuidMap,
			String parentPath) {
		if (METADATA_KEYS.contains(node.getName())) {
			return null;
		}

		Map<String, Object> templateNode = new HashMap<>();
		templateNode.put("id", UUID.randomUUID().toString());
		templateNode.put("name", node.getName());

		List<Map<String, String>> attrs = new ArrayList<>();
		if (node.getAttrs() != null && !node.getAttrs().isEmpty()) {
			for (Map.Entry<String, String> attr : node.getAttrs().entrySet()) {
				String attrName = attr.getKey();
				if (!METADATA_KEYS.contains(attrName)) {
					Map<String, String> attrMap = new HashMap<>();
					attrMap.put("name", attrName);

					String uuid = columnUuidMap.get(attrName);
					attrMap.put("value",
							(node.getChildren() == null || node.getChildren().isEmpty()) && uuid != null
									? "{{" + uuid + "}}"
									: attr.getValue());

					attrs.add(attrMap);
				}
			}
		}
		templateNode.put("attrs", attrs);

		String currentPath = ValidationUtil.isHavingValue(parentPath) ? parentPath + "." + node.getName()
				: node.getName();
		String uuid = columnUuidMap.get(node.getName());
		templateNode.put("value",
				(node.getChildren() == null || node.getChildren().isEmpty()) && uuid != null ? "{{" + uuid + "}}" : "");

		List<Map<String, Object>> children = new ArrayList<>();
		if (node.getChildren() != null && !node.getChildren().isEmpty()) {
			for (XSDNode child : node.getChildren()) {
				Map<String, Object> childNode = convertXsdNodeToTemplateNode(child, columnUuidMap, currentPath);
				if (childNode != null) {
					children.add(childNode);
				}
			}
		}
		templateNode.put("child", children);

		return templateNode;
	}

	/**
	 * Converts XsdNode structure to ColumnConfiguration list for integration with
	 * ColumnConfigurationService.addbulk method. Processes schema nodes to create
	 * column definitions for use in relation and template generation.
	 *
	 * @param rootNode     Root XSD node
	 * @param bu           Business unit
	 * @param platform     Platform name
	 * @param dataCategory Data category
	 * @param sourceKey    Source key
	 * @param userEmail    User email
	 * @param userRole     User role
	 * @return List of ColumnConfiguration objects
	 */
	private List<ColumnConfiguration> convertXsdNodeToColumnConfigurations(XSDNode rootNode, String bu, String platform,
			String dataCategory, String sourceKey, String userEmail, String userRole, String primaryIdentifier) {
		List<ColumnConfiguration> configurations = new ArrayList<>();
		long position = 1;

		// Track columns per source key to avoid primary identifier duplication
		Map<String, Set<String>> sourceKeyColumns = new HashMap<>();

		position = processXsdNodeRecursively(rootNode, configurations, bu, platform, dataCategory, sourceKey, userEmail,
				userRole, "", position, sourceKeyColumns, rootNode.getName(), sourceKey);

		// Add primary identifier only if it doesn't already exist in each source key
		// group
		if (ValidationUtil.isHavingValue(primaryIdentifier)) {
			for (Map.Entry<String, Set<String>> entry : sourceKeyColumns.entrySet()) {
				String uniqueSourceKey = entry.getKey();
				Set<String> existingColumns = entry.getValue();

				// Only add primaryIdentifier if it doesn't already exist
				if (!existingColumns.contains(primaryIdentifier)) {
					ColumnConfiguration primaryIdConfig = new ColumnConfiguration();
					primaryIdConfig.setBu(bu);
					primaryIdConfig.setPlatform(platform);
					primaryIdConfig.setDataCategory(dataCategory);
					primaryIdConfig.setSourceKey(uniqueSourceKey);
					primaryIdConfig.setColumnName(primaryIdentifier);
					primaryIdConfig.setPosition(position++);
					primaryIdConfig.setAliasName(primaryIdentifier);
					primaryIdConfig.setUuid(UUID.randomUUID().toString());
					primaryIdConfig.setRequired("Y");
					primaryIdConfig.setColumnRequiredInErrorFile("N");
					primaryIdConfig.setUseremail(userEmail);
					primaryIdConfig.setUserrole(userRole);
					primaryIdConfig.setPath("@Dynamic@." + primaryIdentifier);
					primaryIdConfig.setDataType(DataType.Text.getDataType());

					configurations.add(primaryIdConfig);

					log.debug(
							"XSDSchemaConfigurationService - convertXsdNodeToColumnConfigurations() - Added primaryIdentifier {} to sourceKey: {}",
							primaryIdentifier, uniqueSourceKey);
				} else {
					log.debug(
							"XSDSchemaConfigurationService - convertXsdNodeToColumnConfigurations() - Skipped primaryIdentifier {} as it already exists in sourceKey: {}",
							primaryIdentifier, uniqueSourceKey);
				}
			}
		}

		return configurations;
	}

	/**
	 * Recursively processes XsdNode to create ColumnConfiguration objects. Handles
	 * both element nodes and attribute nodes, excluding metadata.
	 *
	 * @param node           Current XSD node
	 * @param configurations List to add configurations to
	 * @param bu             Business unit
	 * @param platform       Platform name
	 * @param dataCategory   Data category
	 * @param sourceKey      Source key
	 * @param userEmail      User email
	 * @param userRole       User role
	 * @param parentPath     Parent path for nested elements
	 * @param position       Current position counter
	 * @return Updated position counter
	 */
	private long processXsdNodeRecursively(XSDNode node, List<ColumnConfiguration> configurations, String bu,
			String platform, String dataCategory, String sourceKey, String userEmail, String userRole,
			String parentPath, long position, Map<String, Set<String>> sourceKeyColumns, String rootNodeName, String rootSourceKey) {

		long currentPosition = position;

		if (node == null || !node.isSelectable() || !ValidationUtil.isHavingValue(node.getName())
				|| METADATA_KEYS.contains(node.getName())) {
			return currentPosition;
		}

		// Build hierarchical path
		String fullPath = ValidationUtil.isHavingValue(parentPath) ? parentPath + "." + node.getName() : node.getName();
		String currentSourceKey = sourceKey;

		// Generate source key for nodes with children (groups/containers)
		if (node.getChildren() != null && !node.getChildren().isEmpty()) {
			// Avoid generating redundant root entries like
			// "Workers_Effective_Stack{{Workers_Effective_Stack}}"
			if (!node.getName().equals(rootNodeName) || !ValidationUtil.isHavingValue(parentPath)) {
				// Build proper hierarchical source key
				String hierarchicalPath = buildHierarchicalPath(parentPath, node.getName(), rootNodeName);
				currentSourceKey = rootSourceKey + "{{" + hierarchicalPath + "}}";
			} else {
				// For root node, keep the original source key
				currentSourceKey = sourceKey;
			}
		}

		// Process leaf nodes (elements without children)
		if (ValidationUtil.isHavingValue(currentSourceKey)
				&& (node.getChildren() == null || node.getChildren().isEmpty())) {

			// Ensure we have source key columns tracking
			sourceKeyColumns.computeIfAbsent(currentSourceKey, k -> new HashSet<>());

			ColumnConfiguration config = new ColumnConfiguration();
			config.setBu(bu);
			config.setPlatform(platform);
			config.setDataCategory(dataCategory);
			config.setSourceKey(currentSourceKey);
			config.setColumnName(node.getName());
			config.setPosition(currentPosition++);
			config.setAliasName(node.getName());
			config.setUuid(UUID.randomUUID().toString());
			config.setRequired("Y");
			config.setColumnRequiredInErrorFile("N");
			config.setUseremail(userEmail);
			config.setUserrole(userRole);
			config.setPath(rootNodeName + "." +fullPath);
			config.setDataType(inferDataType(node));

			configurations.add(config);

			// Track this column in the source key
			sourceKeyColumns.get(currentSourceKey).add(node.getName());

			log.debug("XSDSchemaConfigurationService - processXsdNodeRecursively() - Created ColumnConfiguration for element: {}, path: {}, sourceKey: {}, dataType: {}",
					node.getName(), fullPath, currentSourceKey, config.getDataType());
		}

		// Process attributes
		if (node.getAttrs() != null && !node.getAttrs().isEmpty()) {
			for (Map.Entry<String, String> attr : node.getAttrs().entrySet()) {
				String attrName = attr.getKey();
				if (!METADATA_KEYS.contains(attrName)) {

					String attrPath = fullPath + "@" + attrName;

					// Ensure we have source key columns tracking
					sourceKeyColumns.computeIfAbsent(currentSourceKey, k -> new HashSet<>());

					ColumnConfiguration attrConfig = new ColumnConfiguration();
					attrConfig.setBu(bu);
					attrConfig.setPlatform(platform);
					attrConfig.setDataCategory(dataCategory);
					attrConfig.setSourceKey(currentSourceKey);
					attrConfig.setColumnName(attrName);
					attrConfig.setPosition(currentPosition++);
					attrConfig.setAliasName(attrName);
					attrConfig.setUuid(UUID.randomUUID().toString());
					attrConfig.setRequired("Y");
					attrConfig.setColumnRequiredInErrorFile("N");
					attrConfig.setUseremail(userEmail);
					attrConfig.setUserrole(userRole);
					attrConfig.setPath(rootNodeName + "." + attrPath);
					attrConfig.setDataType(inferDataType(node));

					configurations.add(attrConfig);

					// Track this column in the source key
					sourceKeyColumns.get(currentSourceKey).add(attrName);

					log.debug("XSDSchemaConfigurationService - processXsdNodeRecursively() - Created ColumnConfiguration for attribute: {}, path: {}, sourceKey: {}, dataType: {}",
							attrName, attrPath, currentSourceKey, attrConfig.getDataType());
				}
			}
		}

		// Process children recursively
		if (node.getChildren() != null && !node.getChildren().isEmpty()) {
			for (XSDNode child : node.getChildren()) {
				// Don't add the root node name again if we're already at root level
				String childParentPath = node.getName().equals(rootNodeName) && parentPath.isEmpty() ? "" : fullPath;

				currentPosition = processXsdNodeRecursively(child, configurations, bu, platform, dataCategory,
						currentSourceKey, userEmail, userRole, childParentPath, currentPosition, sourceKeyColumns,
						rootNodeName, rootSourceKey);
			}
		}

		return currentPosition;
	}

	/**
	 * Builds hierarchical path for source key generation, avoiding redundant root
	 * entries.
	 */
	private String buildHierarchicalPath(String parentPath, String nodeName, String rootNodeName) {
		if (!ValidationUtil.isHavingValue(parentPath)) {
			// If no parent path and node is not root, start with node name
			return nodeName.equals(rootNodeName) ? "" : nodeName;
		}

		// Remove root node name from parent path if it exists at the beginning
		String cleanParentPath = parentPath.startsWith(rootNodeName + ".")
				? parentPath.substring(rootNodeName.length() + 1)
				: parentPath;

		// If clean parent path is empty or equals root node name, start fresh
		if (!ValidationUtil.isHavingValue(cleanParentPath) || cleanParentPath.equals(rootNodeName)) {
			return nodeName;
		}

		return cleanParentPath + "." + nodeName;
	}

	/**
	 * Processes a template string for runtime usage. Validates the template string
	 * and returns it for runtime processing.
	 *
	 * @param templateString The template string to process
	 * @return The processed template string
	 * @throws ConfigurationException if the template is invalid or blank
	 */
	public String processTemplateForRuntime(String templateString) {
		log.info("XSDSchemaConfigurationService - processTemplateForRuntime() - Processing template string for runtime usage");

		if (!ValidationUtil.isHavingValue(templateString)) {
			throw new ConfigurationException("Template string cannot be blank");
		}

		try {
			objectMapper.readTree(templateString);
			log.info("XSDSchemaConfigurationService - processTemplateForRuntime() - Successfully processed template string");
			return templateString;
		} catch (Exception ex) {
			log.error("XSDSchemaConfigurationService - processTemplateForRuntime() - Failed to process template string: {}",
					ex.getMessage());
			throw new ConfigurationException("Invalid template format: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Utility method to infer DataType enum from XSDNode or attribute. Determines
	 * the most appropriate data type for a given schema node using attribute or
	 * node name heuristics.
	 *
	 * @param node The XSDNode for which data type is to be inferred
	 * @return String representing the inferred data type
	 */
	private String inferDataType(XSDNode node) {
		if (node.getAttrs() != null) {
			String type = node.getAttrs().get("type");
			if (type != null) {
				String lowerType = type.toLowerCase();
				if (lowerType.contains("string"))
					return DataType.Text.getDataType();
				if (lowerType.contains("date"))
					return DataType.Date.getDataType();
				if (lowerType.contains("int") || lowerType.contains("number"))
					return DataType.Number.getDataType();
				if (lowerType.contains("email"))
					return DataType.Email.getDataType();
			}
		}
		String name = node.getName().toLowerCase();
		if (name.contains("date"))
			return DataType.Date.getDataType();
		if (name.contains("email"))
			return DataType.Email.getDataType();
		return DataType.Text.getDataType();
	}
}