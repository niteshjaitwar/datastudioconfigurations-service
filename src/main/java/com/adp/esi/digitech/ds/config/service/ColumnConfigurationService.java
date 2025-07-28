package com.adp.esi.digitech.ds.config.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

//import com.adp.digitech.fileprocessing.v2.validator.service.ExcelMetadataValidatorService;
import com.adp.esi.digitech.ds.config.entity.ColumnRelationEntity;
import com.adp.esi.digitech.ds.config.entity.ConfigDataEntity;
import com.adp.esi.digitech.ds.config.entity.TransformationRulesEntity;
import com.adp.esi.digitech.ds.config.entity.ValidationsRulesEntity;
import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.model.ColumnConfigData;
import com.adp.esi.digitech.ds.config.model.ColumnConfiguration;
import com.adp.esi.digitech.ds.config.model.ColumnConfigurationResponse;
import com.adp.esi.digitech.ds.config.model.ColumnRelation;
import com.adp.esi.digitech.ds.config.model.ConfigurationData;
import com.adp.esi.digitech.ds.config.model.ErrorData;
import com.adp.esi.digitech.ds.config.model.TransformationRule;
import com.adp.esi.digitech.ds.config.model.ValidationInfo;
import com.adp.esi.digitech.ds.config.model.ValidationRule;
import com.adp.esi.digitech.ds.config.repo.ColumnRelationRepository;
import com.adp.esi.digitech.ds.config.repo.ConfigDataRepository;
import com.adp.esi.digitech.ds.config.repo.LOVTypeRepository;
import com.adp.esi.digitech.ds.config.repo.TransformationRulesRepository;
import com.adp.esi.digitech.ds.config.repo.ValidationRulesRepository;
import com.adp.esi.digitech.ds.config.util.ValidationUtil;
import com.adp.esi.digitech.ds.enums.DataType;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ColumnConfigurationService {
	
	@Autowired
	ConfigDataRepository configDataRepository;

	@Autowired
	ColumnRelationRepository columnRelationRepository;

	@Autowired
	ValidationRulesRepository validationRulesRepository;

	@Autowired
	TransformationRulesRepository transformationRulesRepository;
	
	@Autowired
	LOVTypeRepository lovTypeRepository;
	
	@Autowired
	ConfigurationDataService configurationDataService;	
	
	@Autowired
	ColumnRelationService columnRelationService;
	
	@Autowired
	ValidationRulesService validationRulesService;
	
	@Autowired
	TransformationRulesService transformationRulesService;	
	
	@Autowired
	ModelMapper modelMapper;

	@Autowired
	SimpleDateFormat simpleDateFormat;
	
	//@Autowired
	//private ExcelMetadataValidatorService excelMetadataValidatorService;
	
	@Autowired
	DataFormatter dataFormatter;
	
	public ColumnConfigurationResponse findBy(String bu, String platform, String dataCategory) throws Exception {
		
		var configurationData  = findConfigurationDataBy(bu, platform, dataCategory);
		
		ColumnConfigurationResponse columnConfigurationResponse = new ColumnConfigurationResponse();
		columnConfigurationResponse.setConfigurationData(configurationData);	
		
		CompletableFuture<List<ColumnRelation>> columnRelationDataCompletable = CompletableFuture.supplyAsync(()-> {
			return columnRelationService.find(bu, platform, dataCategory);
					/*columnRelationRepository.findByBuAndPlatformAndDataCategory(bu, platform, dataCategory).stream()
					.map(item -> {						
						var temp = modelMapper.map(item, ColumnRelation.class);
						if(ValidationUtil.isHavingValue(item.getModifiedBy()))
							temp.setUseremail(item.getModifiedBy());
						else
							temp.setUseremail(item.getCreatedBy());
						return temp;
					}).collect(Collectors.toList());*/
		});
		
		CompletableFuture<List<ValidationRule>> validationRuleDataCompletable = CompletableFuture.supplyAsync(()-> {
			return validationRulesService.findByList(bu, platform, dataCategory);
			//return validationRulesRepository.findByBuAndPlatformAndDataCategory(bu, platform, dataCategory).stream().map(item -> modelMapper.map(item, ValidationRule.class)).collect(Collectors.toList());
		});
		
		CompletableFuture<List<TransformationRule>> transformationRuleDataCompletable = CompletableFuture.supplyAsync(()-> {
			return transformationRulesService.find(bu, platform, dataCategory);
			//return transformationRulesRepository.findByBuAndPlatformAndDataCategory(bu, platform, dataCategory).stream().map(item -> modelMapper.map(item, TransformationRule.class)).collect(Collectors.toList());
		});
		
		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(columnRelationDataCompletable, validationRuleDataCompletable, transformationRuleDataCompletable);
		combinedFuture.join();
		
		columnConfigurationResponse.setColumnRelations(columnRelationDataCompletable.get());
		columnConfigurationResponse.setValidationRules(validationRuleDataCompletable.get());
		columnConfigurationResponse.setTransformationRules(transformationRuleDataCompletable.get());
		
		//columnConfigurationResponse.setColumnRelations(columnRelationRepository.findByBuAndPlatformAndDataCategory(bu, platform, dataCategory).stream().map(item -> modelMapper.map(item, ColumnRelation.class)).collect(Collectors.toList()));
		//columnConfigurationResponse.setTransformationRules(transformationRulesRepository.findByBuAndPlatformAndDataCategory(bu, platform, dataCategory).stream().map(item -> modelMapper.map(item, TransformationRule.class)).collect(Collectors.toList()));
		//columnConfigurationResponse.setValidationRules(validationRulesRepository.findByBuAndPlatformAndDataCategory(bu, platform, dataCategory).stream().map(item -> modelMapper.map(item, ValidationRule.class)).collect(Collectors.toList()));
		
		return columnConfigurationResponse;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = { ConfigurationException.class })
	public ColumnConfigurationResponse addbulk(List<ColumnConfiguration> columnConfigurations) throws Exception{
		
		if(columnConfigurations == null || columnConfigurations.isEmpty())
			throw new ConfigurationException("No Data Found in the request");
		
		List<ColumnRelationEntity> columnRelationEntities = new ArrayList<>();
		List<ValidationsRulesEntity> validationsRulesEntities = new ArrayList<>();
		List<TransformationRulesEntity> transformationRulesEntities = new ArrayList<>();
		
		columnConfigurations.forEach(item -> {
			if (!ValidationUtil.isHavingValue(item.getUuid()))
				item.setUuid(UUID.randomUUID().toString());

			ColumnRelationEntity columnRelationEntity = new ColumnRelationEntity();
			columnRelationEntity.setBu(item.getBu());
			columnRelationEntity.setPlatform(item.getPlatform());
			columnRelationEntity.setDataCategory(item.getDataCategory());
			columnRelationEntity.setSourceKey(item.getSourceKey());
			columnRelationEntity.setColumnName(item.getColumnName());
			columnRelationEntity.setPosition(item.getPosition());
			columnRelationEntity.setAliasName(item.getAliasName());
			columnRelationEntity.setUuid(item.getUuid());
			columnRelationEntity.setRequired(item.getRequired());
			columnRelationEntity.setColumnRequiredInErrorFile(item.getColumnRequiredInErrorFile());
			columnRelationEntity.setCreatedBy(item.getUseremail());
			columnRelationEntity.setCreatedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			columnRelationEntity.setUserrole(item.getUserrole());
			if (ValidationUtil.isHavingValue(item.getPath()))
				columnRelationEntity.setPath(item.getPath());

			columnRelationEntities.add(columnRelationEntity);

			ValidationsRulesEntity validationsRulesEntity = new ValidationsRulesEntity();
			validationsRulesEntity.setBu(item.getBu());
			validationsRulesEntity.setPlatform(item.getPlatform());
			validationsRulesEntity.setDataCategory(item.getDataCategory());
			validationsRulesEntity.setSourceColumn(item.getUuid());
			validationsRulesEntity.setDataType(DataType.Text.getDataType());
			
			if(!ValidationUtil.isHavingValue(item.getDataType()))					
				validationsRulesEntity.setDataType(item.getDataType());			
			
			validationsRulesEntity.setValidationRuleType("client");
			validationsRulesEntity.setUseremail(item.getUseremail());
			validationsRulesEntity.setUserrole(item.getUserrole());
			validationsRulesEntity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));

			validationsRulesEntities.add(validationsRulesEntity);

			TransformationRulesEntity transformationRulesEntity = new TransformationRulesEntity();
			transformationRulesEntity.setBu(item.getBu());
			transformationRulesEntity.setPlatform(item.getPlatform());
			transformationRulesEntity.setDataCategory(item.getDataCategory());
			transformationRulesEntity.setSourceColumnName(item.getUuid());
			transformationRulesEntity.setTargetColumnName(item.getColumnName());
			transformationRulesEntity.setUseremail(item.getUseremail());
			transformationRulesEntity.setUserrole(item.getUserrole());
			transformationRulesEntity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			transformationRulesEntity.setTargetFileName("<null>");

			transformationRulesEntities.add(transformationRulesEntity);

		});
		/*
		log.info("ColumnConfigurationService - addbulk(), started calling savell");
		CompletableFuture<List<ColumnRelation>> columnRelationDataCompletable = CompletableFuture.supplyAsync(()-> {
			return columnRelationRepository.saveAll(columnRelationEntities).stream()
					.map(item -> {
						var temp = modelMapper.map(item, ColumnRelation.class);
						if(ValidationUtil.isHavingValue(item.getModifiedBy()))
							temp.setUseremail(item.getModifiedBy());
						else
							temp.setUseremail(item.getCreatedBy());
						return temp;
					}).collect(Collectors.toList());
		});
		
		//log.info("ColumnConfigurationService - addbulk(), started calling savell for validationsRulesEntities");
		CompletableFuture<List<ValidationRule>> validationRuleDataCompletable = CompletableFuture.supplyAsync(()-> {
			return validationRulesRepository.saveAll(validationsRulesEntities).stream()
					.map(item -> modelMapper.map(item, ValidationRule.class)).collect(Collectors.toList());
		});
		
		//log.info("ColumnConfigurationService - addbulk(), started calling savell for transformationRulesEntities");
		CompletableFuture<List<TransformationRule>> transformationRuleDataCompletable = CompletableFuture.supplyAsync(()-> {
			return transformationRulesRepository.saveAll(transformationRulesEntities).stream()
					.map(item -> modelMapper.map(item, TransformationRule.class)).collect(Collectors.toList());
		});
		
		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(columnRelationDataCompletable, validationRuleDataCompletable, transformationRuleDataCompletable);
		combinedFuture.join();
		log.info("ColumnConfigurationService - addbulk(), completed calling savell");
		
		ColumnConfigurationResponse columnConfigurationResponse = new ColumnConfigurationResponse();
		columnConfigurationResponse.setColumnRelations(columnRelationDataCompletable.get());
		columnConfigurationResponse.setValidationRules(validationRuleDataCompletable.get());
		columnConfigurationResponse.setTransformationRules(transformationRuleDataCompletable.get());
		 */
		
		ColumnConfigurationResponse columnConfigurationResponse = new ColumnConfigurationResponse();
		
		var tempColumnRelationEntities = columnRelationRepository.saveAll(columnRelationEntities);
		var tempValidationsRulesEntities = validationRulesRepository.saveAll(validationsRulesEntities);
		var temptransformationRulesEntities = transformationRulesRepository.saveAll(transformationRulesEntities);

		
		columnConfigurationResponse.setColumnRelations(tempColumnRelationEntities.stream().map(item -> modelMapper.map(item, ColumnRelation.class)).collect(Collectors.toList()));
		columnConfigurationResponse.setTransformationRules(temptransformationRulesEntities.stream().map(item -> modelMapper.map(item, TransformationRule.class)).collect(Collectors.toList()));
		columnConfigurationResponse.setValidationRules(tempValidationsRulesEntities.stream().map(item -> modelMapper.map(item, ValidationRule.class)).collect(Collectors.toList()));

		return columnConfigurationResponse;

	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = { ConfigurationException.class })
	public ColumnConfigurationResponse saveEntities(String bu, String platform, String dataCategory, MultipartFile columnConfigurationsFile) throws Exception {		
		Workbook workbook  = new XSSFWorkbook(columnConfigurationsFile.getInputStream());
		//excelMetadataValidatorService.validate(columnConfigurationsFile);	
		
		List<ColumnRelationEntity> columnRelationEntities = new ArrayList<>();
		List<ValidationsRulesEntity> validationsRulesEntities = new ArrayList<>();
		List<TransformationRulesEntity> transformationRulesEntities = new ArrayList<>();
		
		try {
			var isMultiSheetsFound = workbook.getNumberOfSheets() == 1;
			IntStream.range(0, workbook.getNumberOfSheets()).parallel().forEach(index -> {				
				org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(index);
				
				var sourceKey = columnConfigurationsFile.getOriginalFilename();
				sourceKey = isMultiSheetsFound ? sourceKey.concat("{{" + sheet.getSheetName() + "}}") : sourceKey;
				
				Iterator<Row> rowIterator = sheet.iterator();
				Row row = rowIterator.next();
				if(!(row.getPhysicalNumberOfCells() == 1 && row.getCell(0).getStringCellValue().equalsIgnoreCase("COLUMN_NAME"))) {					
					throw new ConfigurationException("Missing or invalid headers .The Column Header size is 1 and header is COLUMN_NAME");
				}			
				
				while (rowIterator.hasNext()) {
					Row nextRow = rowIterator.next();
					String uuid = UUID.randomUUID().toString();

					ColumnRelationEntity columnRelationEntity = new ColumnRelationEntity();
					columnRelationEntity.setBu(bu);
					columnRelationEntity.setPlatform(platform);
					columnRelationEntity.setDataCategory(dataCategory);
					columnRelationEntity.setSourceKey(sourceKey);
					columnRelationEntity.setColumnName(dataFormatter.formatCellValue(nextRow.getCell(0)));
					columnRelationEntity.setPosition(Long.parseLong(dataFormatter.formatCellValue(nextRow.getCell(1))));
					columnRelationEntity.setAliasName(dataFormatter.formatCellValue(nextRow.getCell(0)));
					columnRelationEntity.setUuid(uuid);
					columnRelationEntity.setRequired("Y");
					columnRelationEntity.setCreatedBy("DVTS-Admin@adp.com");
					columnRelationEntity.setCreatedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
					columnRelationEntity.setUserrole("DVTS-Admin");

					columnRelationEntities.add(columnRelationEntity);
					

					ValidationsRulesEntity validationsRulesEntity = new ValidationsRulesEntity();
					validationsRulesEntity.setBu(bu);
					validationsRulesEntity.setPlatform(platform);
					validationsRulesEntity.setDataCategory(dataCategory);
					validationsRulesEntity.setSourceColumn(uuid);
					validationsRulesEntity.setDataType("Text");
					validationsRulesEntity.setUseremail("DVTS-Admin@adp.com");
					validationsRulesEntity.setUserrole("DVTS-Admin");
					validationsRulesEntity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));

					validationsRulesEntities.add(validationsRulesEntity);

					TransformationRulesEntity transformationRulesEntity = new TransformationRulesEntity();
					transformationRulesEntity.setBu(bu);
					transformationRulesEntity.setPlatform(platform);
					transformationRulesEntity.setDataCategory(dataCategory);
					transformationRulesEntity.setSourceColumnName(uuid);
					transformationRulesEntity.setTargetColumnName(dataFormatter.formatCellValue(nextRow.getCell(0)));
					transformationRulesEntity.setUseremail("DVTS-Admin@adp.com");
					transformationRulesEntity.setUserrole("DVTS-Admin");
					transformationRulesEntity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
					transformationRulesEntity.setTargetFileName("<null>");

					transformationRulesEntities.add(transformationRulesEntity);
					
				
				}
			
			});
		} finally {
			if(workbook != null)
				workbook.close();
		}
		if(columnRelationEntities == null || columnRelationEntities.isEmpty())
			throw new ConfigurationException("No Column Data Found in the document");
		
		/*
		CompletableFuture<List<ColumnRelation>> columnRelationDataCompletable = CompletableFuture.supplyAsync(()-> {
			return columnRelationRepository.saveAll(columnRelationEntities).stream()
					.map(item -> {
						var temp = modelMapper.map(item, ColumnRelation.class);
						if(ValidationUtil.isHavingValue(item.getModifiedBy()))
							temp.setUseremail(item.getModifiedBy());
						else
							temp.setUseremail(item.getCreatedBy());
						return temp;
					}).collect(Collectors.toList());
		});
		
		CompletableFuture<List<ValidationRule>> validationRuleDataCompletable = CompletableFuture.supplyAsync(()-> {
			return validationRulesRepository.saveAll(validationsRulesEntities).stream()
					.map(item -> modelMapper.map(item, ValidationRule.class)).collect(Collectors.toList());
		});
		
		CompletableFuture<List<TransformationRule>> transformationRuleDataCompletable = CompletableFuture.supplyAsync(()-> {
			return transformationRulesRepository.saveAll(transformationRulesEntities).stream()
					.map(item -> modelMapper.map(item, TransformationRule.class)).collect(Collectors.toList());
		});
		
		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(columnRelationDataCompletable, validationRuleDataCompletable, transformationRuleDataCompletable);
		combinedFuture.join();
		
		ColumnConfigurationResponse columnConfigurationResponse = new ColumnConfigurationResponse();
		columnConfigurationResponse.setColumnRelations(columnRelationDataCompletable.get());
		columnConfigurationResponse.setValidationRules(validationRuleDataCompletable.get());
		columnConfigurationResponse.setTransformationRules(transformationRuleDataCompletable.get());
		*/
		
		ColumnConfigurationResponse columnConfigurationResponse = new ColumnConfigurationResponse();
		var tempColumnRelationEntities = columnRelationRepository.saveAll(columnRelationEntities);
		var tempValidationsRulesEntities = validationRulesRepository.saveAll(validationsRulesEntities);
		var temptransformationRulesEntities = transformationRulesRepository.saveAll(transformationRulesEntities);

	
		columnConfigurationResponse.setColumnRelations(tempColumnRelationEntities.stream().map(item -> modelMapper.map(item, ColumnRelation.class)).collect(Collectors.toList()));
		columnConfigurationResponse.setTransformationRules(temptransformationRulesEntities.stream().map(item -> modelMapper.map(item, TransformationRule.class)).collect(Collectors.toList()));
		columnConfigurationResponse.setValidationRules(tempValidationsRulesEntities.stream().map(item -> modelMapper.map(item, ValidationRule.class)).collect(Collectors.toList()));

		return columnConfigurationResponse;
	}

	public void validateConfigurationData(String bu, String platform, String dataCategory) throws Exception {
		var errorMessages = new ArrayList<ErrorData>();
		try {
			var columnRelationCount = columnRelationRepository.countByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
			var validationRulesCount = validationRulesRepository.countByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
			var transformationRulesCount = transformationRulesRepository.countByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
			var configDataCount = configDataRepository.countByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
			
			if(configDataCount == 0)
				addErrorMessage(errorMessages, "No Configuration found for given request");
			else {
				var configurationData  = findConfigurationDataBy(bu, platform, dataCategory);
				validateRule(errorMessages, "No Input Rules found for given request", "Configured Input Rules are invalid for given request", configurationData.getInputRules());
				validateRule(errorMessages, "No Output Rules found for given request", "Configured Output Rules are invalid for given request", configurationData.getOutputFileRules());
				//validateRule(errorMessages, "No Data Rules found for given request", "Configured Data Rules are invalid for given request", configurationData.getDataRules());
				//validateRule(errorMessages, "No files info found for given request", "Configured files info invalid for given request", configurationData.getFilesInfo());
			}
			
			if(columnRelationCount == 0)
				addErrorMessage(errorMessages, "No Column Relations found for given request");
			
			if(validationRulesCount == 0)
				addErrorMessage(errorMessages, "No Validation Rules found for given request");
			else {
				var validationLovTypes = lovTypeRepository.findDistinctLovTypes(bu, platform, dataCategory);
				if(CollectionUtils.isNotEmpty(validationLovTypes))
					addErrorMessage(errorMessages, String.format("No Lov Data found for lov types %s",validationLovTypes));
			}
			
			if(transformationRulesCount == 0)
				addErrorMessage(errorMessages, "No Transformation Rules found for given request");
			
			if(CollectionUtils.isNotEmpty(errorMessages)) {
				var configurationException = new ConfigurationException("Configurations are not proper for the given request");
				configurationException.setErrors(errorMessages);
				throw configurationException;
			}
		} catch (Exception e) {
			log.error("ColumnConfigurationService - validateConfigurationData() failed to validate configurations for the request. Error Message = {}",  e.getMessage());
			var configurationException =  new ConfigurationException(e.getMessage(), e.getCause());
			configurationException.setErrors(errorMessages);
			throw configurationException;
		}
	}
	
	private ConfigurationData findConfigurationDataBy(String bu, String platform, String dataCategory) throws Exception { 
		Optional<ConfigDataEntity> optinalCofigDataEntity  = configDataRepository.findByBUPlatformAndDataCategory(bu, platform, dataCategory);
		if (optinalCofigDataEntity.isEmpty()) {
			log.error("ColumnConfigurationService - findBy() No config data found of bu = {}, platform = {}, dataCategory = {} ",
					bu, platform, dataCategory);
			throw new ConfigurationException("No config data found");
		}
		ConfigurationData configurationData = null;
		if(optinalCofigDataEntity.isPresent()) {
			ConfigDataEntity configDataEntity = optinalCofigDataEntity.get();
			configurationData = modelMapper.map(configDataEntity, ConfigurationData.class);
		}
		return configurationData;
	}
	
	private void addErrorMessage(List<ErrorData> errorMessages, String message) {
		errorMessages.add(new ErrorData("Configuration Error",message));
	}
	
	private void validateRule(List<ErrorData> errorMessages, String noRulesMsg, String invalidRuleMsg, String rule) {
		if(!ValidationUtil.isHavingValue(rule))
			addErrorMessage(errorMessages, noRulesMsg);
		else if(!ValidationUtil.isValidJsonArray(rule))
			addErrorMessage(errorMessages, invalidRuleMsg);

	}
	
	public List<ValidationInfo> checkValidationFields(String bu, String platform, String dataCategory, List<String> fields) throws ConfigurationException {
		var validations = new ArrayList<ValidationInfo>();
		try {
			var columnRelations = columnRelationService.find(bu, platform, dataCategory);
			Map<String, String> colMap = columnRelations.stream().collect(Collectors.toMap(columnRel -> columnRel.getUuid(), columnRel -> columnRel.getColumnName()));
			var transformationRules = transformationRulesService.find(bu, platform, dataCategory);
			var validationRules = validationRulesService.findByList(bu, platform, dataCategory);

			transformationRules.stream().forEach(transformRule -> {
				if(!colMap.keySet().contains(transformRule.getSourceColumnName())) {
					colMap.put(transformRule.getSourceColumnName(), transformRule.getTargetColumnName());
				}
			});

			if (colMap.isEmpty()) {
				log.error("ColumnConfigurationService - checkValidationFields() No fields found for bu = {}, platform = {}, dataCategory = {} ",bu, platform, dataCategory);
				throw new ConfigurationException("No fields found");
			}

			var configurationData = findConfigurationDataBy(bu, platform, dataCategory);
			var configFileInfo = configurationData.getFilesInfo();
			var configInputRules = configurationData.getInputRules();
			var configOuputRules = configurationData.getOutputFileRules();
			var configDataRules = configurationData.getDataRules();

			var dataSetMap = new HashMap<String, String>();

			JSONArray configInputRulesJsonArray = new JSONArray(configInputRules);
			IntStream.range(0, configInputRulesJsonArray.length()).forEach(index -> {
				JSONObject configInputRulesJson = configInputRulesJsonArray.optJSONObject(index);
				dataSetMap.put(configInputRulesJson.optString("dataSetId"),
						configInputRulesJson.optString("dataSetName"));
			});

			var validationsMap = new HashMap<String, List<String>>();

			validateConfigFileInfo(configFileInfo, fields, validationsMap, dataSetMap);
			validateConfigInputRules(configInputRules, fields, validationsMap);
			validateConfigOuputRules(configOuputRules, fields, validationsMap, dataSetMap);
			validateConfigDataRules(configDataRules, fields, validationsMap);
			validateTransformationRules(transformationRules, fields, validationsMap, colMap, dataSetMap);
			validateValidationRules(validationRules, fields, validationsMap, colMap);

			validationsMap.forEach((key, validationList) -> {
				validations.add(new ValidationInfo(colMap.get(key), validationList));
			});

		} catch (Exception e) {
			log.error("ColumnConfigurationService - checkValidationFields() failed to give warn fields for the request. Error Message = {}",  e.getMessage());
			var configurationException =  new ConfigurationException(e.getMessage(), e.getCause());
			throw configurationException;
		}
		return validations;
		
	}

	private void addErrorMessage(List<String> fields, Map<String, List<String>> validationsMap, String jsonStr, String errorMessage) {
		fields.stream().forEach(field -> {
			if(jsonStr.contains(field))
				validationsMap.computeIfAbsent(field, v -> new ArrayList<String>()).add(errorMessage);
		});
	}
	
	private void validateConfigFileInfo(String configFileInfo, List<String> fields, Map<String, List<String>> validationsMap, Map<String,String> dataSetMap) {
		if(Objects.nonNull(configFileInfo)) {
			JSONArray configFileInfoJsonArray = new JSONArray(configFileInfo);
			IntStream.range(0, configFileInfoJsonArray.length()).forEach(index -> {
				var configFileJson = configFileInfoJsonArray.optJSONObject(index);
				var groupIdentifier = configFileJson.optString("groupIdentifier");
				var sourceKey = configFileJson.optString("sourceKey");
				var type = configFileJson.optString("type");
				if("dataset".equals(type))
					sourceKey = dataSetMap.get(sourceKey);
				else
					sourceKey.replace("{{", " : ").replace("}}", "").trim();
				var errorReferenceMessage = "Remove field from Input Structure Configuration -> "+sourceKey+", lines";
				var errorGroupIdentifierMessage = "Remove field from Data Set Configuration -> File Info -> Source Name: "+sourceKey+", Group Identifier";
				addErrorMessage(fields, validationsMap, groupIdentifier, errorGroupIdentifierMessage);
				var configFileJsonStr = Objects.nonNull(configFileJson) ? configFileJson.toString() : "";
				addErrorMessage(fields, validationsMap, configFileJsonStr, errorReferenceMessage);
			});
		}
	}
	
	private void validateValidationRules(List<ValidationRule> validationRules, List<String> fields, Map<String, List<String>> validationsMap, Map<String, String> colMap) {
		validationRules.forEach(validationRule -> {
			if(Objects.nonNull(validationRule.getConditionalValidationRule())){
				JSONObject validationRuleJson = new JSONObject(validationRule.getConditionalValidationRule());
				var conditionalJsonArray = validationRuleJson.optJSONArray("conditions");
				IntStream.range(0, conditionalJsonArray.length()).forEach(index -> {
					var conditionalJson = conditionalJsonArray.optJSONObject(index);
					var ifConditionArray = conditionalJson.has("if") && !conditionalJson.isNull("if") ? conditionalJson.optJSONArray("if").toString() : null;
					if(ValidationUtil.isHavingValue(ifConditionArray)) {
						var errorMessage = "Remove field from Validation -> Conditional Validation of Source Column: "+colMap.get(validationRule.getSourceColumn())+", If Condition"+(index+1);
						addErrorMessage(fields, validationsMap, ifConditionArray, errorMessage);
					}
				});
			}
		});
	}
	
	private void validateTransformationRules(List<TransformationRule> transformationRules, List<String> fields, Map<String, List<String>> validationsMap, Map<String, String> colMap, Map<String,String> dataSetMap) {
		if(Objects.nonNull(transformationRules)) {
			transformationRules.forEach(transformationRule -> {
				if(Objects.nonNull(transformationRule.getDefaultValue())){
					var errorMessage = "Remove field from Transformation -> Target Fields, for Target Field Name: "+transformationRule.getTargetColumnName();
					addErrorMessage(fields, validationsMap, transformationRule.getDefaultValue(), errorMessage);
				}
					
				if(Objects.nonNull(transformationRule.getDataTransformationRules())){
					JSONArray dataTransformationRulesArray = new JSONArray(transformationRule.getDataTransformationRules());
					
					IntStream.range(0, dataTransformationRulesArray.length()).forEach(index -> {
						var dataTransformationRuleJson = dataTransformationRulesArray.optJSONObject(index);
						var dataSetName = dataSetMap.get(dataTransformationRuleJson.optString("dataSetId"));
						var configurationsJson = dataTransformationRuleJson.optJSONObject("configurations").toString();
						var errorMessage = "Remove field from Transformation -> Configuration -> Data Transformations of Source field: "+colMap.get(transformationRule.getSourceColumnName())+" for Dataset: "+dataSetName;
						addErrorMessage(fields, validationsMap, configurationsJson, errorMessage);
					});
				}
			});
		}	
	}
	
	private void validateConfigDataRules(String configDataRules, List<String> fields, Map<String, List<String>> validationsMap) {
		if(Objects.nonNull(configDataRules)) {
			JSONArray configDataRulesJsonArray = new JSONArray(configDataRules);
			IntStream.range(0, configDataRulesJsonArray.length()).forEach(index -> {
				var configDataRulesJson = configDataRulesJsonArray.optJSONObject(index);
				var dataSetName = configDataRulesJson.optString("dataSetName");
				var filterJson = configDataRulesJson.has("filters") && !configDataRulesJson.isNull("filters") ? configDataRulesJson.optJSONObject("filters").toString(): null;
				if(Objects.nonNull(filterJson)) {
					var errorMessage = "Remove field from Data Rules, Dataset: "+dataSetName+", Filter";
					addErrorMessage(fields, validationsMap, filterJson, errorMessage);
				}
				
				var groupByJson = configDataRulesJson.has("groupBy") && !configDataRulesJson.isNull("groupBy") ? configDataRulesJson.optJSONObject("groupBy"): null;
				if(Objects.nonNull(groupByJson)) {
					var groupByColumns = groupByJson.has("columns") && !groupByJson.isNull("columns") ? groupByJson.optJSONArray("columns").toString() : null;
					if(Objects.nonNull(groupByColumns)) {
						var errorMessage = "Remove field from Data Rules, Dataset: "+dataSetName+", Group By, Columns";
						addErrorMessage(fields, validationsMap, groupByColumns, errorMessage);
					}
					var groupByClause = groupByJson.has("clause") && !groupByJson.isNull("clause") ? groupByJson.optJSONArray("clause").toString() : null;
					if(Objects.nonNull(groupByClause)) {
						var errorMessage = "Remove field from Data Rules, Dataset: "+dataSetName+", Group By, Clause";
						addErrorMessage(fields, validationsMap, groupByClause, errorMessage);
					}
					var groupByFilter = groupByJson.has("filters") && !groupByJson.isNull("filters") ? groupByJson.optJSONArray("filters").toString() : null;
					if(Objects.nonNull(groupByFilter)) {
						var errorMessage = "Remove field from Data Rules, Dataset: "+dataSetName+", Group By, Filter";
						addErrorMessage(fields, validationsMap, groupByFilter, errorMessage);
					}
					var groupByAggregations = groupByJson.has("aggregations") && !groupByJson.isNull("aggregations") ? groupByJson.optJSONArray("aggregations").toString() : null;
					if(Objects.nonNull(groupByAggregations)) {
						var errorMessage = "Remove field from Data Rules, Dataset: "+dataSetName+", Group By, Aggregations";
						addErrorMessage(fields, validationsMap, groupByAggregations, errorMessage);
					}
				}
			});	
		}
	}
	
	private void validateConfigInputRules(String configInputRules, List<String> fields, Map<String, List<String>> validationsMap) {
		if(ValidationUtil.isHavingValue(configInputRules)) {
			JSONArray configInputRulesArray = new JSONArray(configInputRules);
			IntStream.range(0, configInputRulesArray.length()).forEach(index -> {
				var configInputRulesJson = configInputRulesArray.optJSONObject(index);
				var referencesJsonArray = configInputRulesJson.has("references") && !configInputRulesJson.isNull("references") ? configInputRulesJson.optJSONArray("references") : null;
				if(Objects.nonNull(referencesJsonArray)){
					IntStream.range(0, referencesJsonArray.length()).forEach(refInd -> {
						var referenceJson = referencesJsonArray.optJSONObject(refInd);
						var sourceKey = referenceJson.optString("sourceKey");
						var keyIdentifiersArray = configInputRulesJson.has("keyIdentifiers") && !configInputRulesJson.isNull("keyIdentifiers") ? configInputRulesJson.optJSONArray("keyIdentifiers") : null;
						sourceKey.replace("{{", " : ").replace("}}", "").trim();
						if(Objects.nonNull(keyIdentifiersArray)){
							IntStream.range(0, keyIdentifiersArray.length()).forEach(keyIndex -> {
								var keyIdentifiersJson = keyIdentifiersArray.optJSONObject(keyIndex);
								var referenceIdentifier = keyIdentifiersJson.optString("referenceIdentifier");
								var parentIdentifier = keyIdentifiersJson.optString("parentIdentifier");
								var errorReferenceMessage = "Remove field from Input Configuration -> References, Reference File: "+sourceKey+", Reference Field";
								addErrorMessage(fields, validationsMap, referenceIdentifier, errorReferenceMessage);
								
								var errorParentMessage = "Remove field from Input Configuration -> References, Reference File: "+sourceKey+", Source Field";
								addErrorMessage(fields, validationsMap, parentIdentifier, errorParentMessage);
							});
						}
					});
				}
			});	
		}
	}
	
	private void validateConfigOuputRules(String configOuputRules, List<String> fields, Map<String, List<String>> validationsMap, Map<String,String> dataSetMap) {
		if(Objects.nonNull(configOuputRules)) {
			JSONArray outputFileRulesJsonArray = new JSONArray(configOuputRules);
			IntStream.range(0, outputFileRulesJsonArray.length()).forEach(index -> {
				var outputFileRuleJson = outputFileRulesJsonArray.optJSONObject(index);
				var outputFileType = outputFileRuleJson.optString("outputFileType");
				var fieldataSet = outputFileRuleJson.optString("dataSetName");
				var fileNameJson = outputFileRuleJson.optJSONObject("fileName");
				var prefixArray = fileNameJson.optJSONArray("columns");
				var suffixArray = fileNameJson.optJSONArray("suffix");
				IntStream.range(0, prefixArray.length()).forEach(ind -> {
					var prefix = prefixArray.optString(ind);
					var errorPrefixMessage = "Remove field from Output Configuration of type: "+outputFileType+", Dataset: "+dataSetMap.get(fieldataSet)+", prefix";
					addErrorMessage(fields, validationsMap, prefix, errorPrefixMessage);
				});
				IntStream.range(0, suffixArray.length()).forEach(ind -> {
					var suffix = suffixArray.optString(ind);
					var errorSuffixMessage = "Remove field from Output Configuration of type: "+outputFileType+", Dataset: "+dataSetMap.get(fieldataSet)+", suffix";
					addErrorMessage(fields, validationsMap, suffix, errorSuffixMessage);
				});
				
				if("xlsx".equalsIgnoreCase(outputFileType)) {
					var sheetsArray = outputFileRuleJson.optJSONArray("sheets");
					IntStream.range(0, sheetsArray.length()).forEach(sheetIndex -> {
						JSONObject sheetJSONObject = sheetsArray.optJSONObject(sheetIndex);
						var headerJsonArrayStr = sheetJSONObject.getJSONArray("headers").toString();
						var errorMessage = "Remove field from Output Configuration of type: "+outputFileType+", Sheet"+(sheetIndex+1)+" Header";
						addErrorMessage(fields, validationsMap, headerJsonArrayStr, errorMessage);
						
						var sheetNameJson = sheetJSONObject.optJSONObject("sheetName");
						var sheetPrefixArray = sheetNameJson.optJSONArray("columns");
						var sheetSuffixArray = sheetNameJson.optJSONArray("suffix");
						IntStream.range(0, sheetPrefixArray.length()).forEach(ind -> {
							var prefix = sheetPrefixArray.optString(ind);
							var errorPrefixMessage = "Remove field from Output Configuration of type: "+outputFileType+", Sheet"+(sheetIndex+1)+", prefix";
							addErrorMessage(fields, validationsMap, prefix, errorPrefixMessage);
						});
						
						IntStream.range(0, sheetSuffixArray.length()).forEach(ind -> {
							var suffix = sheetSuffixArray.optString(ind);
							var errorSuffixMessage = "Remove field from Output Configuration of type: "+outputFileType+", Sheet"+(sheetIndex+1)+", suffix";
							addErrorMessage(fields, validationsMap, suffix, errorSuffixMessage);
						});
					});
					
				}	
				if("csv".equalsIgnoreCase(outputFileType)) {
					var headerJsonArrayStr = outputFileRuleJson.getJSONArray("fields").toString();
					var errorMessage = "Remove field from Output Configuration of type: "+outputFileType+" Header";
					addErrorMessage(fields, validationsMap, headerJsonArrayStr, errorMessage);
				}	
				if("txt".equalsIgnoreCase(outputFileType)) {
					var dataArray = outputFileRuleJson.optJSONArray("data");
					IntStream.range(0, dataArray.length()).forEach(lineIndex -> {
						JSONObject lineJSONObject = dataArray.optJSONObject(lineIndex);
						var referenceId = lineJSONObject.optString("referenceId");
						var errorMessage = "Remove field from Output Configuration of type: "+outputFileType+", Line"+(lineIndex+1)+", Reference";
						addErrorMessage(fields, validationsMap, referenceId, errorMessage);
						
						var headerJsonArrayStr = lineJSONObject.getJSONArray("col").toString();
						var errorsg = "Remove field from Output Configuration of type: "+outputFileType+", Line"+(lineIndex+1);
						addErrorMessage(fields, validationsMap, headerJsonArrayStr, errorsg);
					});						
				}	
				if("xml".equalsIgnoreCase(outputFileType)) {
					var xmlTemplateJSONObj = outputFileRuleJson.optJSONObject("xmlTemplate");
					var rowArrayJSON = xmlTemplateJSONObj.optJSONArray("child");
					IntStream.range(0, rowArrayJSON.length()).forEach(rowIndex -> {
						var rowJSONObject = rowArrayJSON.optJSONObject(rowIndex);
						var rowFiled = rowJSONObject.optString("value");
						var errorMessage = "Remove field from Output Configuration of type: "+outputFileType+", Row"+(rowIndex+1);
						addErrorMessage(fields, validationsMap, rowFiled, errorMessage);

						
						var attrJsonArray = rowJSONObject.getJSONArray("attrs");
						IntStream.range(0, attrJsonArray.length()).forEach(attrIndex -> {
							var attrJSONObject = attrJsonArray.optJSONObject(attrIndex);
							var attrFiled = attrJSONObject.optString("value");
							var errormsg = "Remove field from Output Configuration of type: "+outputFileType+", Row"+(rowIndex+1)+", Attribute "+(attrIndex+1);
							addErrorMessage(fields, validationsMap, attrFiled, errormsg);

						});	
					});						
				}	
			});
		}
	}
	
	private void updateFilesInfoAndInputRules(ColumnConfigData columnData, List<ColumnRelation> columnRelationList)
			throws Exception {

		Set<String> uuidSet = Set.copyOf(columnData.getUuids());
		
		var allFieldsDeletedSourceKeys = columnRelationList.stream()
				.collect(Collectors.groupingBy(ColumnRelation::getSourceKey,
						Collectors.mapping(ColumnRelation::getUuid, Collectors.toSet())))
				.entrySet().stream().filter(entry -> uuidSet.containsAll(entry.getValue())).map(Map.Entry::getKey)
				.toList();
		
		var configData = findConfigurationDataBy(columnData.getBu(), columnData.getPlatform(),
				columnData.getDataCategory());
		JSONArray configInputRulesArray = new JSONArray(configData.getInputRules());
		JSONArray configFileInfoJsonArray = new JSONArray(configData.getFilesInfo());
		JSONArray updatedConfigInputRulesArray = new JSONArray();
		
		IntStream.range(0, configInputRulesArray.length())
				.filter(inputRulesIndex -> !allFieldsDeletedSourceKeys
						.contains(configInputRulesArray.getJSONObject(inputRulesIndex).optString("sourceKey")))
				.forEach(index -> updatedConfigInputRulesArray.put(configInputRulesArray.getJSONObject(index)));

		var updatedConfigFileInfoJsonArray = IntStream.range(0, configFileInfoJsonArray.length())
				.mapToObj(i -> configFileInfoJsonArray.get(i)).map(obj -> {
					JSONObject item = (JSONObject) obj;
					String sourceKey = item.getString("sourceKey");
					boolean isSourceDeleted = allFieldsDeletedSourceKeys.stream()
							.anyMatch(key -> item.has("lines") ? key.split("\\{\\{")[0].equals(sourceKey)
									: key.equals(sourceKey));
					if (isSourceDeleted && item.has("lines")) {
						JSONArray lines = item.getJSONArray("lines");
						JSONArray linesToKeep = new JSONArray(IntStream.range(0, lines.length())
								.mapToObj(j -> lines.getJSONObject(j)).filter(lineObj -> {
									String lineKey = sourceKey + "{{" + lineObj.getString("lineName") + "}}";
									return !allFieldsDeletedSourceKeys.contains(lineKey);
								}).collect(Collectors.toList()));
						if (linesToKeep.length() > 0) {
							return item;
						}
						return null; // skip if all lines are deleted
					} else if (!isSourceDeleted) {
						return item; // keep if sourceKey is not deleted
					}
					return null; // skip if sourceKey is deleted AND either (no lines to process for text type)
									// OR (type is like excel,JSON,CSV,form data)
				}).filter(Objects::nonNull).collect(Collectors.toList());
		
		var configObject = Map.of("id", configData.getId().toString(), 
			   "inputRules", updatedConfigInputRulesArray.toString(), 
			   "filesInfo", updatedConfigFileInfoJsonArray.toString(), 
			   "useremail", columnData.getUseremail(),
			   "userrole", columnData.getUserrole());
		
		configurationDataService.patch(List.of(configObject));
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = { ConfigurationException.class })
	public List<ValidationInfo> softDeletefield(ColumnConfigData columnData) throws Exception{
		var validations = checkValidationFields(columnData.getBu(), columnData.getPlatform(),columnData.getDataCategory(),columnData.getUuids());
		
		if(validations.isEmpty()) {
			var columnRelationList = columnRelationService.find(columnData.getBu(), columnData.getPlatform(),columnData.getDataCategory());
			columnRelationRepository.softDeletefield(columnData.getBu(), columnData.getPlatform(),columnData.getDataCategory(),columnData.getUuids(),columnData.getUseremail(),columnData.getUserrole());
			validationRulesRepository.softDeletefield(columnData.getBu(), columnData.getPlatform(),columnData.getDataCategory(),columnData.getUuids(),columnData.getUseremail(),columnData.getUserrole());
			transformationRulesRepository.softDeletefield(columnData.getBu(), columnData.getPlatform(),columnData.getDataCategory(),columnData.getUuids(),columnData.getUseremail(),columnData.getUserrole());
			updateFilesInfoAndInputRules(columnData,columnRelationList);
		}
		return validations;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = { ConfigurationException.class })
	public void softDeleteDataCategory(ConfigurationData configData) throws Exception{
		
		findConfigurationDataBy(configData.getBu(), configData.getPlatform(),configData.getDataCategory());
		
		configDataRepository.softDeleteByBuAndPlatformAndDataCategory(configData.getBu(), configData.getPlatform(),configData.getDataCategory(),configData.getUseremail(),configData.getUserrole());
		columnRelationRepository.softDeleteByBuAndPlatformAndDataCategory(configData.getBu(), configData.getPlatform(),configData.getDataCategory(),configData.getUseremail(),configData.getUserrole());
		validationRulesRepository.softDeleteByBuAndPlatformAndDataCategory(configData.getBu(), configData.getPlatform(),configData.getDataCategory(),configData.getUseremail(),configData.getUserrole());
		transformationRulesRepository.softDeleteByBuAndPlatformAndDataCategory(configData.getBu(), configData.getPlatform(),configData.getDataCategory(),configData.getUseremail(),configData.getUserrole());

	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = { ConfigurationException.class })
	public void deleteByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory){
		
		configDataRepository.deleteByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
		columnRelationRepository.deleteByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
		validationRulesRepository.deleteByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
		transformationRulesRepository.deleteByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = { ConfigurationException.class }) 
	public void updateDataCategory(List<Map<String, String>>  configurations) throws Exception {
		
		boolean isFound = configurations.parallelStream().allMatch(data -> data.containsKey("id") && ValidationUtil.isHavingValue((String)data.get("id")));
		if(!isFound)
			throw new ConfigurationException("id is mandatory and id should not be empty or null ");
		
		List<Long> ids = configurations.parallelStream().map(map -> Long.valueOf((String)map.get("id"))).collect(Collectors.toList());
		var entities = configDataRepository.findAllById(ids);
		List<Long> entityIds = entities.parallelStream().map(entity -> entity.getId()).toList();	
		
		var idList = ids.parallelStream().filter(id -> !entityIds.contains(id)).toList();
				
		if(idList.size() > 0)
			throw new ConfigurationException("No data found for given Id's " + idList.toString());
		
		entities.parallelStream().forEach(entity -> {
			Optional<Map<String, String>> optional = configurations.stream()
					.filter(data -> entity.getId().compareTo(Long.valueOf((String) data.get("id"))) == 0).findFirst();

			if (optional.isPresent()) {
				Map<String,String> configMap = optional.get();
				var newDataCategory = configMap.get("dataCategory"); var userEmail = configMap.get("useremail"); var userRole = configMap.get("userrole");	
				var configData = modelMapper.map(entity, ConfigurationData.class);
				var bu = configData.getBu(); var platform = configData.getPlatform(); var dataCategory = configData.getDataCategory();
				var fileinfoStr = configData.getFilesInfo();
				var inputRulesStr = configData.getInputRules();

				if (ValidationUtil.isHavingValue(fileinfoStr) && fileinfoStr.contains(dataCategory)) {
					JSONArray configFileInfoJsonArray = new JSONArray(fileinfoStr);
					for (int i = 0; i < configFileInfoJsonArray.length(); i++) {
						var configFileJson = configFileInfoJsonArray.optJSONObject(i);
						var sourceKey = configFileJson.optString("sourceKey");
						sourceKey = sourceKey.contains("{{") ?  sourceKey.substring(0, sourceKey.indexOf("{{")) : sourceKey;
						var type = configFileJson.optString("type");
						if ("json".equals(type) && sourceKey.equals(dataCategory)) {
							fileinfoStr = fileinfoStr.replace(dataCategory, newDataCategory);
							if (ValidationUtil.isHavingValue(inputRulesStr) && inputRulesStr.contains(dataCategory)) {
								inputRulesStr = inputRulesStr.replace(dataCategory, newDataCategory);
							}
							columnRelationRepository.updateSourceKey(bu, platform, dataCategory, newDataCategory, userEmail, userRole);
							break;
						}
					}
				}

				configDataRepository.updateDataCategory(bu, platform, dataCategory, newDataCategory, fileinfoStr, inputRulesStr, userEmail, userRole);
				columnRelationRepository.updateDataCategory(bu, platform, dataCategory, newDataCategory, userEmail, userRole);
				validationRulesRepository.updateDataCategory(bu, platform, dataCategory, newDataCategory, userEmail, userRole);
				transformationRulesRepository.updateDataCategory(bu, platform, dataCategory, newDataCategory, userEmail, userRole);

			}

		});	
		
	}
}
