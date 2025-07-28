package com.adp.esi.digitech.ds.config.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.model.ColumnConfigurationResponse;
import com.adp.esi.digitech.ds.config.model.DVTSVersioning;
import com.adp.esi.digitech.ds.config.model.ErrorData;
import com.adp.esi.digitech.ds.config.util.EncryptionUtils;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConfigurationImportService {

	@Autowired
	ColumnConfigurationService columnConfigurationService;

	@Autowired
	ColumnRelationService columnRelationService;

	@Autowired
	TransformationRulesService transformationRulesService;

	@Autowired
	ValidationRulesService validationRulesService;

	@Autowired
	ConfigurationDataService configurationDataService;
	
	@Autowired
	EncryptionUtils encryptionUtils;
	
	@Autowired
	ObjectMapper objectMapper;

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {ConfigurationException.class})
	public void importData(DVTSVersioning versioning, MultipartFile file)
			throws StreamReadException, DatabindException, IOException {
		log.info("ConfigurationImportService - importData() started importing data of bu = {}, platform = {}, dataCategory = {}",versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());

		// Read JSON data from the file and map it to object
		var encryptedData = new String(file.getBytes());
		var decryptedData = encryptionUtils.decrypt(encryptedData);
		
		log.info("ConfigurationImportService - importData() completed decrypting data of bu = {}, platform = {}, dataCategory = {}",versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());
		ColumnConfigurationResponse columnConfigurationResponse = objectMapper.readValue(decryptedData,	ColumnConfigurationResponse.class);

		importData(versioning, columnConfigurationResponse);
		log.info("ConfigurationImportService - importData() completed importing data of bu = {}, platform = {}, dataCategory = {}", versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {ConfigurationException.class})
	public void revertData(DVTSVersioning versioning) throws StreamReadException, DatabindException, IOException {
		log.info("ConfigurationImportService - revertData() started reverting data of bu = {}, platform = {}, dataCategory = {}", versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());

		// Read JSON data from the file and map it to object
		ColumnConfigurationResponse columnConfigurationResponse = objectMapper.readValue(versioning.getDvtsConfigData(), ColumnConfigurationResponse.class);
		importData(versioning, columnConfigurationResponse);
		log.info("ConfigurationImportService - revertData() completed reverting data of bu = {}, platform = {}, dataCategory = {}",	versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());
	}

	
	private void importData(DVTSVersioning versioning, ColumnConfigurationResponse columnConfigurationResponse) {
		var errors = new ArrayList<ErrorData>();
		
		if (ObjectUtils.isEmpty(columnConfigurationResponse.getColumnRelations())) {
			errors.add(new ErrorData("Configuration Error", "No Column Relations found for given request"));
		}
		
		if (ObjectUtils.isEmpty(columnConfigurationResponse.getTransformationRules())) {
			errors.add(new ErrorData("Configuration Error", "No Transformation Rules found for given request"));
		}
		if (ObjectUtils.isEmpty(columnConfigurationResponse.getValidationRules())) {
			errors.add(new ErrorData("Configuration Error", "No Validation Rules found for given request"));
		}
		if (ObjectUtils.isEmpty(columnConfigurationResponse.getConfigurationData())) {
			errors.add(new ErrorData("Configuration Error", "No Configuration Data found for given request"));
		}

		if (!ObjectUtils.isEmpty(errors)) {
			var configurationException = new ConfigurationException("Configurations are not proper for the given request");
			
			log.info("ConfigurationImportService - importData() failed with errors bu = {}, platform = {}, dataCategory = {}, errors = {}",versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory(), errors);
			throw configurationException;
		}

		var configurationData = columnConfigurationResponse.getConfigurationData();
		var deleteColumnRelationsIds = columnRelationService.findIdsListBy(versioning.getBu(),	versioning.getPlatform(), versioning.getDataCategory());
		var deleteTransformationRulesIds = transformationRulesService.findIdsListBy(versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());
		var deleteValidationRulesIds = validationRulesService.findIdsListBy(versioning.getBu(),versioning.getPlatform(), versioning.getDataCategory());
		

		if (Objects.nonNull(deleteColumnRelationsIds) && !deleteColumnRelationsIds.isEmpty()) {			
			log.info("ConfigurationImportService - importData() column relations id's found for bu = {}, platform = {}, dataCategory = {}, id's = {}",versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory(), deleteColumnRelationsIds.size());
			columnRelationService.deleteBulk(deleteColumnRelationsIds);
			log.info("ConfigurationImportService - importData() completed deleting column relation data of bu = {}, platform = {}, dataCategory = {}",versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());
		}
		if (Objects.nonNull(deleteTransformationRulesIds) && !deleteTransformationRulesIds.isEmpty()) {
			log.info("ConfigurationImportService - importData() transformation rules id's found for bu = {}, platform = {}, dataCategory = {}, id's = {}",versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory(), deleteTransformationRulesIds.size());
			transformationRulesService.deleteBulk(deleteTransformationRulesIds);
			log.info("ConfigurationImportService - importData() completed deleting transformation rules data of bu = {}, platform = {}, dataCategory = {}",versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());
		}
		if (Objects.nonNull(deleteValidationRulesIds) && !deleteValidationRulesIds.isEmpty()) {
			log.info("ConfigurationImportService - importData() validation rules id's found for bu = {}, platform = {}, dataCategory = {}, id's = {}",versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory(), deleteValidationRulesIds.size());
			validationRulesService.deleteBulk(deleteValidationRulesIds);
			log.info("ConfigurationImportService - importData() completed deleting validation rules data of bu = {}, platform = {}, dataCategory = {}",versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());
		}
		
		try {
			var configData = configurationDataService.findBy(versioning.getBu(),versioning.getPlatform(), versioning.getDataCategory());
			if (Objects.nonNull(configData)) {
				configurationDataService.deleteBulk(Arrays.asList(configData.getId()));
				log.info("ConfigurationImportService - importData() completed deleting config data of bu = {}, platform = {}, dataCategory = {}",versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());
			}
		} catch (ConfigurationException e) {
			log.info("ConfigurationImportService - importData() config data not found for bu = {}, platform = {}, dataCategory = {}",versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());		
		}
		
		
		configurationData.setId(null);
		configurationData.setBu(versioning.getBu());
		configurationData.setPlatform(versioning.getPlatform());
		configurationData.setDataCategory(versioning.getDataCategory());		
		configurationData.setUseremail(versioning.getUseremail());
		configurationData.setUserrole(versioning.getUserrole());

		var columnRelations = columnConfigurationResponse.getColumnRelations().stream().map(item -> {
			item.setId(null);
			item.setBu(versioning.getBu());
			item.setPlatform(versioning.getPlatform());
			item.setDataCategory(versioning.getDataCategory());			
			item.setUseremail(versioning.getUseremail());
			item.setUserrole(versioning.getUserrole());
			return item;
		}).collect(Collectors.toList());

		var validationRules = columnConfigurationResponse.getValidationRules().stream().map(item -> {
			item.setId(null);
			item.setBu(versioning.getBu());
			item.setPlatform(versioning.getPlatform());
			item.setDataCategory(versioning.getDataCategory());
			item.setUseremail(versioning.getUseremail());
			item.setUserrole(versioning.getUserrole());
			return item;
		}).collect(Collectors.toList());

		var transformationRules = columnConfigurationResponse.getTransformationRules().stream().map(item -> {
			item.setId(null);
			item.setBu(versioning.getBu());
			item.setPlatform(versioning.getPlatform());
			item.setDataCategory(versioning.getDataCategory());
			item.setUseremail(versioning.getUseremail());
			item.setUserrole(versioning.getUserrole());
			return item;
		}).collect(Collectors.toList());

		configurationDataService.saveEntity(configurationData);
		log.info("ConfigurationImportService - importData() completed saving config data of bu = {}, platform = {}, dataCategory = {}",versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());
		
		if(!columnRelations.isEmpty()) {
			columnRelationService.saveEntities(columnRelations);
			log.info("ConfigurationImportService - importData() completed saving column relation data of bu = {}, platform = {}, dataCategory = {}",versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());
		}
		validationRulesService.saveEntities(validationRules);
		log.info("ConfigurationImportService - importData() completed saving validation rules data of bu = {}, platform = {}, dataCategory = {}",versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());
		transformationRulesService.saveEntities(transformationRules);
		log.info("ConfigurationImportService - importData() completed saving transformation rules data of bu = {}, platform = {}, dataCategory = {}",versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());
	}

}
