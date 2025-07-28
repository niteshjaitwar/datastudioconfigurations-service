package com.adp.esi.digitech.ds.config.service;

import java.lang.reflect.Field;
import java.sql.Clob;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import com.adp.esi.digitech.ds.config.entity.TransformationRulesEntity;
import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.model.TargetDataFormat;
import com.adp.esi.digitech.ds.config.model.TransformationRule;
import com.adp.esi.digitech.ds.config.repo.TransformationRulesRepository;
import com.adp.esi.digitech.ds.config.util.DBUtils;
import com.adp.esi.digitech.ds.config.util.ValidationUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransformationRulesService extends AbstractConfigService<TransformationRule> {

	@Autowired
	TransformationRulesRepository transformationRulesRepository;
	
	@Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
	int batchSize;

	@Override
	@Transactional(readOnly = true)
	public List<TransformationRule> findAll() {		
		try(var entities = transformationRulesRepository.findAllTransformationRules()) {
			return entities.map(entity -> {
    			var item = modelMapper.map(entity, TransformationRule.class);
    			entityManager.detach(entity);
    			return item;
    		}).collect(Collectors.toList());
		}
	}

	@Override
	public TransformationRule findById(Long id) {
		if(id == null || id.longValue() <= 0)
			throw new ConfigurationException("id should be greater than 0");
		
		TransformationRulesEntity entity = transformationRulesRepository.findById(id)
				.orElseThrow(() -> new ConfigurationException("No data found for given id = "+ id.longValue()));		
		
		return modelMapper.map(entity, TransformationRule.class);
	}
	
	public Map<UUID,TransformationRule> findBy(String bu, String platform, String dataCategory) {
		//var entities = transformationRulesRepository.findByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
		//return entities.stream().map(item -> modelMapper.map(item, TransformationRule.class)).collect(Collectors.toMap(item -> UUID.fromString(item.getSourceColumnName()) , Function.identity()));
		return find(bu, platform, dataCategory).stream().collect(Collectors.toMap(item -> UUID.fromString(item.getSourceColumnName()) , Function.identity()));
	}
	
	public List<TransformationRule> find(String bu, String platform, String dataCategory) {
		log.info("TransformationRulesService - find()  Started Retrieving Data By. bu = {}, platform = {}, dataCategory = {}", bu, platform, dataCategory);
		var entities = transformationRulesRepository.findByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
		return entities.parallelStream().map(result -> {			
			return TransformationRule.builder()
					.id(((Number) result[0]).longValue())
					.bu((String)result[1])
					.platform((String)result[2])
					.dataCategory((String)result[3])
					.sourceColumnName((String)result[4])
					.targetColumnName((String)result[5])
					.targetFileName((String)result[6])
					.columnSequence(Objects.nonNull(result[7])?((Number) result[7]).intValue(): null)
					.useremail((String)result[8])					
					.userrole((String)result[9])
					.defaultValue(Objects.nonNull(result[10])? DBUtils.convertClobToString((Clob)result[10]):null)
					.subDataCategory((String)result[11])				
					.dataTransformationRules(Objects.nonNull(result[12])? DBUtils.convertClobToString((Clob)result[12]):null)
					.specialCharToBeRemoved(Objects.nonNull(result[13])? DBUtils.convertClobToString((Clob)result[13]):null)
					.transformationRequired((String)result[14])
					.dataType((String)result[15])
					.lovCheckType((String)result[16])
					.transformationRequired((String)result[17])
					.dataFormat((String)result[18])
					.lovValidationRequired((String)result[19])
					.dependsOn((String)result[20])
					.build();
		}).collect(Collectors.toList());
	}
	
	@Cacheable(value = "TransformationRulesCache", key = "#bu.concat('_').concat(#platform).concat('_').concat(#dataCategory)")
	public List<TransformationRule> findByListForBatch(String bu, String platform, String dataCategory) {
		log.info("TransformationRulesService - findByListForBatch()  Started Retrieving Data By. bu = {}, platform = {}, dataCategory = {}", bu, platform, dataCategory);
		//var entities = transformationRulesRepository.findByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
		//return entities.stream().map(item -> modelMapper.map(item, TransformationRule.class)).collect(Collectors.toList());
		return findByList(bu, platform, dataCategory);
	}
	
	public List<TransformationRule> findByList(String bu, String platform, String dataCategory) {
		log.info("TransformationRulesService - findByList()  Started Retrieving Data By. bu = {}, platform = {}, dataCategory = {}", bu, platform, dataCategory);
		//var entities = transformationRulesRepository.findByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
		//return entities.stream().map(item -> modelMapper.map(item, TransformationRule.class)).collect(Collectors.toList());
		return find(bu, platform, dataCategory);
	}
	
	public List<Long> findIdsListBy(String bu, String platform, String dataCategory) {
		return transformationRulesRepository.findIdByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
	}

	@Override
	public TransformationRule saveEntity(TransformationRule transformationRule) {
		TransformationRulesEntity transformationEntity = modelMapper.map(transformationRule, TransformationRulesEntity.class);
		transformationEntity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
		transformationEntity.setSourceColumnName(UUID.randomUUID().toString());
		var temp = transformationRulesRepository.save(transformationEntity);
		return modelMapper.map(temp, TransformationRule.class);
	}
	
	@Override
	public List<TransformationRule> saveEntities(List<TransformationRule> transformationRules) {	
		if(transformationRules == null || transformationRules.isEmpty())
			throw new ConfigurationException("Invalid Data, Transformation Rules can't be null or empty");
		var transformationRulesEntities = transformationRules.parallelStream().map(item -> {
			var entity = modelMapper.map(item, TransformationRulesEntity.class);
			//entity.setId(null);
			if(!ValidationUtil.isHavingValue(entity.getSourceColumnName()))
				entity.setSourceColumnName(UUID.randomUUID().toString());
			
			entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			return entity;					
		}).collect(Collectors.toList());
		//log.info("TransformationRulesService - saveEntities() entities = {}", transformationRulesEntities);
		//var batchSize = 50;
		var transformationRulesLength = transformationRulesEntities.size();
		List<TransformationRulesEntity> temp = new ArrayList<>();
		for (int i = 0; i < transformationRulesLength ; i = i + batchSize) {
		    if( i+ batchSize > transformationRulesLength){
		       var tempTransformationRuleEntities = transformationRulesEntities.subList(i, transformationRulesLength);
		       temp.addAll(transformationRulesRepository.saveAll(tempTransformationRuleEntities));
		       break;
		    }
		    var tempTransformationRuleEntities = transformationRulesEntities.subList(i, i + batchSize);
		    temp.addAll(transformationRulesRepository.saveAll(tempTransformationRuleEntities));
		    log.info("TransformationRulesService - saveEntities()  Completed saving partial tempTransformationRuleEntities, i = {}", i + batchSize);
		}
		log.info("TransformationRulesService - saveEntities()  Completed saving Transformation Rules, size = {}", temp.size());
		//var temp = transformationRulesRepository.saveAll(transformationRulesEntities);
		return temp.parallelStream().map(item -> modelMapper.map(item, TransformationRule.class)).collect(Collectors.toList());
	}	

	@Override
	public TransformationRule updateSingle(TransformationRule transformationRule) {
		
		Optional<TransformationRulesEntity> optionaltransformationRulesEntity = transformationRulesRepository.findById(transformationRule.getId());
		if (optionaltransformationRulesEntity.isEmpty())
			throw new ConfigurationException("Invalid Data, No data found for given id = "+ transformationRule.getId());
		var entity = modelMapper.map(transformationRule, TransformationRulesEntity.class);
		entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
		
		var temp = transformationRulesRepository.save(entity);
		return modelMapper.map(temp, TransformationRule.class);
	}

	@Override
	public List<TransformationRule> updateBulk(List<TransformationRule> transformationRules) {
		List<TransformationRulesEntity> entities = transformationRules.parallelStream().map(item -> {
			var entity = modelMapper.map(item, TransformationRulesEntity.class);
			entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			return entity;
		}).collect(Collectors.toList());
		
		List<Long> ids = transformationRules.parallelStream().map(rule -> rule.getId()).collect(Collectors.toList());
		
		var tempEntities = transformationRulesRepository.findAllById(ids);
		
		var notFoundIds = transformationRules.stream().filter(rule -> !tempEntities.stream().anyMatch(entity -> entity.getId().compareTo(rule.getId()) == 0)).map(rule -> rule.getId()).collect(Collectors.toList());
		if (!notFoundIds.isEmpty())
			throw new ConfigurationException("Invalid Data, No data found for given ids = "+ notFoundIds.stream().map(item -> String.valueOf(item)).collect(Collectors.joining(",")));
		Map<Long, TransformationRulesEntity> existing = tempEntities.parallelStream().collect(Collectors.toMap(TransformationRulesEntity::getId, Function.identity()));
		Map<Long, TransformationRulesEntity> newmap = entities.parallelStream().collect(Collectors.toMap(TransformationRulesEntity::getId, Function.identity()));
		existing.putAll(newmap);
		List<TransformationRulesEntity> transformationRulesNew = existing.values().stream().collect(Collectors.toList());
		var temp = transformationRulesRepository.saveAll(transformationRulesNew);
		return temp.parallelStream().map(item -> modelMapper.map(item, TransformationRule.class)).collect(Collectors.toList());
	}

	@Override
	public List<TransformationRule> patch(List<Map<String, String>> transformations) {
		
		var isFound = transformations.parallelStream().allMatch(rowMap -> rowMap.containsKey("id") && ValidationUtil.isHavingValue((String)rowMap.get("id")));
		if(!isFound)
			throw new ConfigurationException("id is mandatory and id should not be empty or null ");
		
		List<Long> ids = transformations.parallelStream().map(rowMap -> Long.valueOf((String)rowMap.get("id"))).collect(Collectors.toList());
		var originalEntities = transformationRulesRepository.findAllById(ids);
		
		if(originalEntities == null || originalEntities.isEmpty())
			throw new ConfigurationException("No data found for given Id's" + ids.toString());
		
		var modifiedEntities = originalEntities.stream().map(entity -> {
			Optional<Map<String, String>> optional = transformations.stream()
																.filter(validation -> entity.getId().compareTo(Long.valueOf((String)validation.get("id"))) == 0)
																.findFirst();
			if(optional.isPresent()) {
				var mapRow = optional.get();
				mapRow.forEach((key, value) -> {
					if(ValidationUtil.isHavingValue(key) && !key.equalsIgnoreCase("id")) {
						Field field = ReflectionUtils.findField(TransformationRulesEntity.class, key);
						field.setAccessible(true);
						if(key.equalsIgnoreCase("columnSequence") && NumberUtils.isParsable(value))
							ReflectionUtils.setField(field, entity, Integer.parseInt(value));
						else
							ReflectionUtils.setField(field, entity, value);
					}
				});
				entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			}
			
			return entity;
		}).collect(Collectors.toList());
		
		var updatedEntities= transformationRulesRepository.saveAll(modifiedEntities);
		return updatedEntities.parallelStream().map(item -> modelMapper.map(item, TransformationRule.class)).collect(Collectors.toList());
	}

	@Override
	public void deleteSingle(Long id) {
		transformationRulesRepository.deleteById(id);
	}

	@Override
	public void deleteBulk(List<Long> ids) {
		transformationRulesRepository.deleteAllByIdInBatch(ids);
	}
	@Transactional
	public void deleteByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory) {
		transformationRulesRepository.deleteByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
	}

	public Long countByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory) {
		return transformationRulesRepository.countByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
	}
	
	public Map<UUID, TargetDataFormat> getTargetFormatMap(String bu, String platform, String dataCategory, String globalDataSetUuid) {
		var transformationRules = findByList(bu, platform, dataCategory);
		var targetFormatMap = new HashMap<UUID, TargetDataFormat>();  
		transformationRules.parallelStream().forEach(transformationRule -> {
			if ((ValidationUtil.isHavingValue(transformationRule.getDataTransformationRules()) && ValidationUtil
					.isContainsDatasetRules(transformationRule.getDataTransformationRules(), globalDataSetUuid))
					|| (ValidationUtil.isHavingValue(transformationRule.getTargetFileName())
							&& transformationRule.getTargetFileName().equals(globalDataSetUuid)
							&& ValidationUtil.isHavingValue(transformationRule.getDefaultValue()))) {
				
				JSONObject rulesJson = null;
				if(ValidationUtil.isHavingValue(transformationRule.getDataTransformationRules()))
					rulesJson = ValidationUtil.getDatasetRules(transformationRule.getDataTransformationRules(), UUID.fromString(globalDataSetUuid));
				else
					rulesJson = new JSONObject(transformationRule.getDefaultValue());
				var targetDataFormat = initTargetDataFormat(rulesJson);
				if(Objects.nonNull(targetDataFormat)) 
					targetFormatMap.put(UUID.fromString(transformationRule.getSourceColumnName()), targetDataFormat);
			}
		});
		return MapUtils.isEmpty(targetFormatMap) ? null : targetFormatMap;
	}
	
	private TargetDataFormat initTargetDataFormat(JSONObject rules) {
		if(Objects.nonNull(rules) &&  rules.has("target_format") && !rules.isNull("target_format")) {
			var targetFormatJson = rules.getJSONObject("target_format");
			var targetType = targetFormatJson.has("targetType") && !targetFormatJson.isNull("targetType") ? targetFormatJson.getString("targetType") : null;
			var targetFormat = targetFormatJson.has("targetFormat") && !targetFormatJson.isNull("targetFormat") ? targetFormatJson.getString("targetFormat") : null;
			var targetDecimalAllowed = targetFormatJson.has("targetDecimalAllowed") && !targetFormatJson.isNull("targetDecimalAllowed") ? targetFormatJson.getString("targetDecimalAllowed") : null;
			if(Objects.nonNull(targetType)) {
				var targetDataFormatObj = new TargetDataFormat();
				targetDataFormatObj.setTargetType(targetType);
				if(Objects.nonNull(targetFormat)) targetDataFormatObj.setTargetFormat(targetFormat);
				targetDataFormatObj.setTargetDecimalAllowed(targetDecimalAllowed);
				return targetDataFormatObj;
			}
		}
		return null;
	}


}
