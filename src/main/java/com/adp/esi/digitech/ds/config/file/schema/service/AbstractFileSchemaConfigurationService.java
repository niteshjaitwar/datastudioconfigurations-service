package com.adp.esi.digitech.ds.config.file.schema.service;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.adp.esi.digitech.ds.config.entity.FileSchemaConfigurationEntity;
import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.model.ErrorData;
import com.adp.esi.digitech.ds.config.model.FileSchemaConfiguration;
import com.adp.esi.digitech.ds.config.repo.FileSchemaConfigurationRepository;
import com.adp.esi.digitech.ds.config.repo.specification.FileSchemaConfigurationSpecification;
import com.adp.esi.digitech.ds.config.service.AbstractConfigService;
import com.adp.esi.digitech.ds.config.service.ColumnConfigurationService;
import com.adp.esi.digitech.ds.config.util.ValidationUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractFileSchemaConfigurationService extends AbstractConfigService<FileSchemaConfiguration> {
	
	
	public static String DEFAULT_VERSION_NAME = "main";
	
	public FileSchemaConfigurationRepository fileSchemaConfigurationRepository;
	
	public ColumnConfigurationService columnConfigurationService;
	
	
	@Autowired
	protected void setFileSchemaConfigurationRepository(FileSchemaConfigurationRepository fileSchemaConfigurationRepository) {
		this.fileSchemaConfigurationRepository = fileSchemaConfigurationRepository;
	}
	
	@Autowired
	protected void setColumnConfigurationService(ColumnConfigurationService columnConfigurationService) {
		this.columnConfigurationService = columnConfigurationService;
	}

	/**
	 * Uploads and parses a new MS FPS schema file. Upserts by sourceKey. If an
	 * entry with sourceKey exists, updates originalFileJson and resets
	 * selectedFileJson. Else, creates a new record.
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
	
	public abstract FileSchemaConfiguration processSchema(String bu, String platform, String dataCategory,
			String sourceKey, MultipartFile file, String useremail, String userrole);
	
	/**
	 * Returns all saved schema configurations.
	 *
	 * @return List of MS FPS schema DTOs
	 */
	@Override
	@Transactional(readOnly = true)
	public List<FileSchemaConfiguration> findAll() {
		log.info("AbstractFileSchemaConfigurationService - findAll() - Retrieving all MS FPS schema configurations");
		try(var entities = fileSchemaConfigurationRepository.findAllFileSchemaConfigurations()) {
			return entities.map(entity -> {
    			var item = modelMapper.map(entity, FileSchemaConfiguration.class);
    			entityManager.detach(entity);
    			return item;
    		}).collect(Collectors.toList());
		}
	}
	
	/**
	 * Fetches a schema by its primary key ID.
	 *
	 * @param id Schema ID
	 * @return DTO, or null if not found
	 */
	@Override
	public FileSchemaConfiguration findById(Long id) {
		if (id == null) {
			throw new ConfigurationException("ID cannot be null");
		}
		log.info("AbstractFileSchemaConfigurationService - findById() - Retrieving MS FPS schema configuration by ID: {}",id);
		return fileSchemaConfigurationRepository.findById(id)
				.map(this::toDto)
				.orElseThrow(() -> new ConfigurationException("No data found for given id = "+ id.longValue()));
	}
	
	public List<FileSchemaConfiguration> findBy(String bu, String platform, String dataCategory, String sourceKey, String version) {
        var spec = this.toSpec(bu, platform, dataCategory, sourceKey, version);
        return fileSchemaConfigurationRepository.findAll(spec).stream().map(this::toDto).collect(Collectors.toList());
	}

	
	@Override
	public FileSchemaConfiguration saveEntity(FileSchemaConfiguration data) {
		var entity = fileSchemaConfigurationRepository.findOne(this.toSpec(data.getBu(), data.getPlatform(), data.getDataCategory(), data.getSourceKey(), DEFAULT_VERSION_NAME))
		.map(existing -> {
			// Update existing: always reset selectedFileJson on re-upload
			existing.setOriginalFile(data.getOriginalFile());
			existing.setOriginalFileJson(data.getOriginalFileJson());
			//existing.setSelectedFileJson(null);
			//existing.setTemplate(null);
			//existing.setVersion(newVersion);
			existing.setModifiedBy(data.getUseremail());
			existing.setUserrole(data.getUserrole());
			return existing;
		})
		.orElseGet(() -> FileSchemaConfigurationEntity.builder().bu(data.getBu()).platform(data.getPlatform())
				.dataCategory(data.getDataCategory()).sourceKey(data.getSourceKey()).originalFile(data.getOriginalFile())
				.originalFileJson(data.getOriginalFileJson()).selectedFileJson(null).template(null)
				.version(DEFAULT_VERSION_NAME).createdBy(data.getUseremail()).modifiedBy(data.getUseremail()).userrole(data.getUserrole()).build());
		FileSchemaConfigurationEntity saved = fileSchemaConfigurationRepository.save(entity);
		log.info("AbstractFileSchemaConfigurationService - saveEntity() - Successfully uploaded MS FPS schema with ID: {}",	saved.getId());
		return toDto(saved);
	}

	/**
	 * Unsupported: Bulk save is not supported.
	 */
	@Override
	public List<FileSchemaConfiguration> saveEntities(List<FileSchemaConfiguration> data) {
		throw new UnsupportedOperationException("Bulk save not supported.");
	}

	/**
	 * Unsupported: Use uploadMsFpsSchema() instead.
	 */
	@Override
	public FileSchemaConfiguration updateSingle(FileSchemaConfiguration data) {
		throw new UnsupportedOperationException("Use uploadSchema instead.");
	}

	/**
	 * Unsupported: Bulk update is not supported.
	 */
	@Override
	public List<FileSchemaConfiguration> updateBulk(List<FileSchemaConfiguration> data) {
		throw new UnsupportedOperationException("Bulk update not supported.");
	}

	/**
	 * Unsupported: Patch operation is not supported.
	 */
	@Override
	public List<FileSchemaConfiguration> patch(List<Map<String, String>> configurations) {
		log.info("AbstractFileSchemaConfigurationService - patch() - Started patching configurations");
		
		var isFound = configurations.parallelStream().allMatch(rowMap -> rowMap.containsKey("id") && ValidationUtil.isHavingValue((String)rowMap.get("id")));
		if(!isFound)
			throw new ConfigurationException("id is mandatory and id should not be empty or null ");
		
		List<Long> ids = configurations.parallelStream().map(rowMap -> Long.valueOf((String)rowMap.get("id"))).collect(Collectors.toList());
		var originalEntities = fileSchemaConfigurationRepository.findAllById(ids);
		
		if(originalEntities == null || originalEntities.isEmpty())
			throw new ConfigurationException("No data found for given Id's" + ids.toString());
		
		var modifiedEntities = originalEntities.stream().map(entity -> {
			Optional<Map<String, String>> optional = configurations.stream()
																.filter(validation -> entity.getId().compareTo(Long.valueOf((String)validation.get("id"))) == 0)
																.findFirst();
			if(optional.isPresent()) {
				var mapRow = optional.get();
				mapRow.forEach((key, value) -> {
					if(ValidationUtil.isHavingValue(key) && !key.equalsIgnoreCase("id")  && !key.equalsIgnoreCase("useremail")) {
						Field field = ReflectionUtils.findField(FileSchemaConfigurationEntity.class, key);
						field.setAccessible(true);
						ReflectionUtils.setField(field, entity, value);
					}
					if(key.equalsIgnoreCase("useremail"))
						entity.setModifiedBy(value);
				});
				
				entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			}
			
			return entity;
		}).collect(Collectors.toList());
		
		var updatedEntities= fileSchemaConfigurationRepository.saveAll(modifiedEntities);
		log.info("AbstractFileSchemaConfigurationService - patch() - Successfully patched {} configurations", updatedEntities.size());
		return updatedEntities.parallelStream().map(item -> {
			var temp = modelMapper.map(item, FileSchemaConfiguration.class);
			if(ValidationUtil.isHavingValue(item.getModifiedBy()))
				temp.setUseremail(item.getModifiedBy());
			else
				temp.setUseremail(item.getCreatedBy());
			return temp;
		
		}).collect(Collectors.toList());
	}

	/**
	 * Deletes a schema by its primary key.
	 *
	 * @param id Schema ID
	 */
	@Override
	@Transactional
	public void deleteSingle(Long id) {
		if (id == null) {
			throw new ConfigurationException("ID cannot be null");
		}
		
		log.info("AbstractFileSchemaConfigurationService - deleteSingle() - Deleting MS FPS schema configuration with ID: {}", id);

		if (!fileSchemaConfigurationRepository.existsById(id)) {
			throw new ConfigurationException("Schema configuration not found with ID: " + id);
		}

		fileSchemaConfigurationRepository.deleteById(id);
		log.info("AbstractFileSchemaConfigurationService - deleteSingle() - Successfully deleted MS FPS schema configuration with ID: {}", id);
	}
	
	/**
	 * Deletes multiple schemas by their IDs.
	 *
	 * @param ids List of schema IDs to delete
	 */
	@Override
	@Transactional
	public void deleteBulk(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			throw new ConfigurationException("IDs list cannot be null or empty");
		}

		log.info("AbstractFileSchemaConfigurationService - deleteBulk() - Bulk deleting MS FPS schema configurations with IDs: {}",	ids);

		List<FileSchemaConfigurationEntity> entities = fileSchemaConfigurationRepository.findAllById(ids);
		if (entities.size() != ids.size()) {
			throw new ConfigurationException("Some schema configurations not found for provided IDs");
		}

		fileSchemaConfigurationRepository.deleteAllById(ids);
		log.info("AbstractFileSchemaConfigurationService - deleteBulk() - Successfully bulk deleted {} MS FPS schema configurations", ids.size());
	}
	
	@Transactional
	public void deleteBy(String bu, String platform, String dataCategory, String sourceKey, String version) {		
		log.info("AbstractFileSchemaConfigurationService - deleteBy() - Deleting configurations by parameters");
		fileSchemaConfigurationRepository.delete(this.toSpec(bu, platform, dataCategory, sourceKey, version));
	}

	
	/**
	 * Validates required parameters.
	 *
	 * @param bu           Business unit
	 * @param platform     Platform name
	 * @param dataCategory Data category
	 * @param sourceKey    Source key (can be null for some operations)
	 * @throws ConfigurationException if validation fails
	 */
	
	@SuppressWarnings("unused")
	private void validateParameters(String bu, String platform, String dataCategory, String sourceKey) {
		if (!ValidationUtil.isHavingValue(bu)) {
			throw new ConfigurationException("Business unit cannot be blank");
		}
		if (!ValidationUtil.isHavingValue(platform)) {
			throw new ConfigurationException("Platform cannot be blank");
		}
		if (!ValidationUtil.isHavingValue(dataCategory)) {
			throw new ConfigurationException("Data category cannot be blank");
		}
		if (sourceKey != null && !ValidationUtil.isHavingValue(sourceKey)) {
			throw new ConfigurationException("Source key cannot be blank when provided");
		}
	}
	
	/**
	 * Validates the uploaded MS FPS file.
	 *
	 * @param file File to validate
	 * @throws ConfigurationException if validation fails
	 */
	@SuppressWarnings("unused")
	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ConfigurationException("MS FPS file is required.");
		}
		String originalFileName = file.getOriginalFilename();
		if (!ValidationUtil.isHavingValue(originalFileName)) {
			throw new ConfigurationException("File name is required.");
		}
		// Accept various file types for MS FPS files
		String lowerFileName = originalFileName.toLowerCase();
		if (!lowerFileName.endsWith(".xsd") && !lowerFileName.endsWith(".xml") && !lowerFileName.endsWith(".json")) {
			throw new ConfigurationException("Only XSD, XML, or JSON files are allowed for MS FPS schema.");
		}
	}
	
	/**
	 * Combined validation method that collects all errors before throwing exception
	 */
	public void validateParametersAndFile(String bu, String platform, String dataCategory, String sourceKey,
			MultipartFile file, String useremail, String userrole) {

		List<ErrorData> errors = new ArrayList<>();

		// Parameter validations
		if (!ValidationUtil.isHavingValue(bu)) {
			errors.add(new ErrorData("VALIDATION_ERROR", "Business unit cannot be blank"));
		}
		if (!ValidationUtil.isHavingValue(platform)) {
			errors.add(new ErrorData("VALIDATION_ERROR", "Platform cannot be blank"));
		}
		if (!ValidationUtil.isHavingValue(dataCategory)) {
			errors.add(new ErrorData("VALIDATION_ERROR", "Data category cannot be blank"));
		}
		if (!ValidationUtil.isHavingValue(sourceKey)) {
			errors.add(new ErrorData("VALIDATION_ERROR", "Source key cannot be blank"));
		}
		if (!ValidationUtil.isHavingValue(useremail)) {
			errors.add(new ErrorData("VALIDATION_ERROR", "User email cannot be blank"));
		}
		if (!ValidationUtil.isHavingValue(userrole)) {
			errors.add(new ErrorData("VALIDATION_ERROR", "User role cannot be blank"));
		}

		// File validations
		if (file == null || file.isEmpty()) {
			errors.add(new ErrorData("VALIDATION_ERROR", "MS FPS file is required"));
		} else {
			String originalFileName = file.getOriginalFilename();
			if (!ValidationUtil.isHavingValue(originalFileName)) {
				errors.add(new ErrorData("VALIDATION_ERROR", "File name is required"));
			} else {
				String lowerFileName = originalFileName.toLowerCase();
				if (!lowerFileName.endsWith(".xsd") && !lowerFileName.endsWith(".xml")
						&& !lowerFileName.endsWith(".json")) {
					errors.add(new ErrorData("VALIDATION_ERROR",
							"Only XSD, XML, or JSON files are allowed for MS FPS schema"));
				}
			}
		}

		// Throw exception with all errors if any exist
		if (!errors.isEmpty()) {
			ConfigurationException exception = new ConfigurationException("Validation failed");
			exception.setErrors(errors);
			throw exception;
		}
	}
	
	/**
	 * Maps an entity to DTO.
	 *
	 * @param entity Entity to map
	 * @return DTO
	 */
	public FileSchemaConfiguration toDto(FileSchemaConfigurationEntity entity) {
		var data =  modelMapper.map(entity, FileSchemaConfiguration.class);
		if(ValidationUtil.isHavingValue(entity.getModifiedBy()))
			data.setUseremail(entity.getModifiedBy());
		else
			data.setUseremail(entity.getCreatedBy());
		return data;
	}

	public Specification<FileSchemaConfigurationEntity> toSpec(String bu, String platform, String dataCategory, String sourceKey, String version) {
		Specification<FileSchemaConfigurationEntity> spec = Specification.where(null);
		
		
		if (ValidationUtil.isHavingValue(bu)) {
            spec = spec.and(FileSchemaConfigurationSpecification.hasBu(bu));
        }

        if (ValidationUtil.isHavingValue(platform)) {
            spec = spec.and(FileSchemaConfigurationSpecification.hasPlatform(platform));
        }

        if (ValidationUtil.isHavingValue(dataCategory)) {
            spec = spec.and(FileSchemaConfigurationSpecification.hasDataCategory(dataCategory));
        }
        
        if (ValidationUtil.isHavingValue(sourceKey)) {
            spec = spec.and(FileSchemaConfigurationSpecification.hasSourceKey(sourceKey));
        }
        
        if (ValidationUtil.isHavingValue(version)) {
            spec = spec.and(FileSchemaConfigurationSpecification.hasVersion(version));
        }
        
        return spec;
	}

}
