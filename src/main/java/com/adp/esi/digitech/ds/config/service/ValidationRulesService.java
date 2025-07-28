package com.adp.esi.digitech.ds.config.service;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import com.adp.esi.digitech.ds.config.entity.ValidationsRulesEntity;
import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.model.ValidationRule;
import com.adp.esi.digitech.ds.config.repo.ValidationRulesRepository;
import com.adp.esi.digitech.ds.config.util.ValidationUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ValidationRulesService extends AbstractConfigService<ValidationRule> {
	@Autowired
	ValidationRulesRepository validationRulesRepository;
	
	@Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
	int batchSize;

	@Override
	@Transactional(readOnly = true)
	public List<ValidationRule> findAll() {		
		try(var entities = validationRulesRepository.findAllValidationRules()) {
			return entities.map(entity -> {
    			var item = modelMapper.map(entity, ValidationRule.class);
    			entityManager.detach(entity);
    			return item;
    		}).collect(Collectors.toList());
		}
	}

	@Override
	public ValidationRule findById(Long id) {
		if(id == null || id.longValue() <= 0)
			throw new ConfigurationException("id should be greater than 0");		
		var entity = validationRulesRepository.findById(id).orElseThrow(() -> new ConfigurationException("No data found for given id = "+ id.longValue()));		
		return modelMapper.map(entity, ValidationRule.class);
	}
	
	public Map<String,List<ValidationRule>> findBy(String bu, String platform, String dataCategory) {
		var entities = validationRulesRepository.findByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
		return entities.stream().filter(item -> !"DELETED".equalsIgnoreCase(item.getStatus())).map(item -> modelMapper.map(item, ValidationRule.class)).collect(Collectors.groupingBy(ValidationRule::getValidationRuleType));
	}
	
	public List<ValidationRule> findByList(String bu, String platform, String dataCategory) {
		var entities = validationRulesRepository.findByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
		return entities.stream().filter(item -> !"DELETED".equalsIgnoreCase(item.getStatus())).map(item -> modelMapper.map(item, ValidationRule.class)).collect(Collectors.toList());
	}
	
	public List<Long> findIdsListBy(String bu, String platform, String dataCategory) {
		return validationRulesRepository.findIdByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
	}
	
	
	@Cacheable(value = "ValidationRulesCache", key = "#bu.concat('_').concat(#platform).concat('_').concat(#dataCategory).concat('_').concat(#validationRuleType)")
	public List<ValidationRule> findByListForBatch(String bu, String platform, String dataCategory, String validationRuleType) {
		log.info("ValidationRulesService - findByListForBatch()  Started Retrieving Data By. bu = {}, platform = {}, dataCategory = {}, ruleType = {}", bu, platform, dataCategory, validationRuleType);
		return findByList(bu, platform, dataCategory, validationRuleType);
	}
	
	public List<ValidationRule> findByList(String bu, String platform, String dataCategory, String validationRuleType) {
		log.info("ValidationRulesService - findByList()  Started Retrieving Data By. bu = {}, platform = {}, dataCategory = {}, ruleType = {}", bu, platform, dataCategory, validationRuleType);
		var entities = validationRulesRepository.findByBuPlatformAndDataCategory(bu, platform, dataCategory, validationRuleType);
		return entities.stream().map(item -> modelMapper.map(item, ValidationRule.class)).collect(Collectors.toList());
	}
	
	public Map<UUID,ValidationRule> findBy(String bu, String platform, String dataCategory, String validationRuleType) {
		var entities = findByList(bu, platform, dataCategory, validationRuleType);
		return entities.stream().map(item -> modelMapper.map(item, ValidationRule.class)).collect(Collectors.toMap(item -> UUID.fromString(item.getSourceColumn()), Function.identity()));
	}

	@Override
	public ValidationRule saveEntity(ValidationRule validationRule) {
		ValidationsRulesEntity validationEntity = modelMapper.map(validationRule, ValidationsRulesEntity.class);
		validationEntity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
		var temp = validationRulesRepository.save(validationEntity);
		return modelMapper.map(temp, ValidationRule.class);
	}

	@Override
	public List<ValidationRule> saveEntities(List<ValidationRule> validationRules) {	
		if(validationRules == null || validationRules.isEmpty())
			throw new ConfigurationException("Invalid Data, Validation Rules can't be null or empty");
		var validationRulesEntities = validationRules.parallelStream().map(item -> {
			var entity = modelMapper.map(item, ValidationsRulesEntity.class);
			entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			return entity;					
		}).collect(Collectors.toList());
		
		//var batchSize = 50;
		var validationRulesLength = validationRulesEntities.size();
		List<ValidationsRulesEntity> temp = new ArrayList<>();
		for (int i = 0; i < validationRulesLength ; i = i + batchSize) {
		    if( i+ batchSize > validationRulesLength){
		       var tempValidationRulesEntities = validationRulesEntities.subList(i, validationRulesLength);
		       temp.addAll(validationRulesRepository.saveAll(tempValidationRulesEntities));
		       break;
		    }
		    var tempValidationRulesEntities = validationRulesEntities.subList(i, i + batchSize);
		    temp.addAll(validationRulesRepository.saveAll(tempValidationRulesEntities));
		    log.info("ValidationRulesService - saveEntities()  Completed saving partial tempValidationRulesEntities, i = {}", i + batchSize);
		}
		log.info("ValidationRulesService - saveEntities()  Completed saving Validation Rules, size = {}", temp.size());
		//var temp = validationRulesRepository.saveAll(validationRulesEntities);
		return temp.parallelStream().map(item -> modelMapper.map(item, ValidationRule.class)).collect(Collectors.toList());
	}	

	@Override
	public ValidationRule updateSingle(ValidationRule validationRule) {
		
		Optional<ValidationsRulesEntity> optionalValidationsRulesEntity = validationRulesRepository.findById(validationRule.getId());
		if (optionalValidationsRulesEntity.isEmpty())
			throw new ConfigurationException("Invalid Data, No data found for given id = "+ validationRule.getId());
		var entity = modelMapper.map(validationRule, ValidationsRulesEntity.class);
		entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
		
		var temp = validationRulesRepository.save(entity);
		return modelMapper.map(temp, ValidationRule.class);
	}
	
	@Override
	public List<ValidationRule> updateBulk(List<ValidationRule> validationRules) {
		List<ValidationsRulesEntity> entities = validationRules.parallelStream().map(item -> {
			var entity = modelMapper.map(item, ValidationsRulesEntity.class);
			entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			return entity;
		}).collect(Collectors.toList());
		
		List<Long> ids = validationRules.parallelStream().map(rule -> rule.getId()).collect(Collectors.toList());
		
		var tempEntities = validationRulesRepository.findAllById(ids);
		
		var notFoundIds = validationRules.stream().filter(rule -> !tempEntities.stream().anyMatch(entity -> entity.getId().compareTo(rule.getId()) == 0)).map(rule -> rule.getId()).collect(Collectors.toList());
		if (!notFoundIds.isEmpty())
			throw new ConfigurationException("Invalid Data, No data found for given ids = "+ notFoundIds.stream().map(item -> String.valueOf(item)).collect(Collectors.joining(",")));
		Map<Long, ValidationsRulesEntity> existing = tempEntities.parallelStream().collect(Collectors.toMap(ValidationsRulesEntity::getId, Function.identity()));
		Map<Long, ValidationsRulesEntity> newmap = entities.parallelStream().collect(Collectors.toMap(ValidationsRulesEntity::getId, Function.identity()));
		existing.putAll(newmap);
		List<ValidationsRulesEntity> validationRulesNew = existing.values().stream().collect(Collectors.toList());
		var temp = validationRulesRepository.saveAll(validationRulesNew);
		return temp.parallelStream().map(item -> modelMapper.map(item, ValidationRule.class)).collect(Collectors.toList());
	}

	@Override
	public List<ValidationRule> patch(List<Map<String, String>> validations) {
		
		var isFound = validations.parallelStream().allMatch(rowMap -> rowMap.containsKey("id") && ValidationUtil.isHavingValue((String)rowMap.get("id")));
		if(!isFound)
			throw new ConfigurationException("id is mandatory and id should not be empty or null");
		
		List<Long> ids = validations.parallelStream().map(rowMap -> Long.valueOf((String)rowMap.get("id"))).collect(Collectors.toList());
		var originalEntities = validationRulesRepository.findAllById(ids);
		
		if(originalEntities == null || originalEntities.isEmpty())
			throw new ConfigurationException("No data found for given Id's" + ids.toString());
		var modifiedEntities = originalEntities.stream().map(entity -> {
			Optional<Map<String, String>> optional = validations.stream()
																.filter(validation -> entity.getId().compareTo(Long.valueOf((String)validation.get("id"))) == 0)
																.findFirst();
			if(optional.isPresent()) {
				var mapRow = optional.get();
				mapRow.forEach((key, value) -> {
					if(ValidationUtil.isHavingValue(key) && !key.equalsIgnoreCase("id")) {
						Field field = ReflectionUtils.findField(ValidationsRulesEntity.class, key);
						field.setAccessible(true);
						ReflectionUtils.setField(field, entity, value);
					}
				});
				entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			}
			
			return entity;
		}).collect(Collectors.toList());
		
		var updatedEntities= validationRulesRepository.saveAll(modifiedEntities);
		return updatedEntities.parallelStream().map(item -> modelMapper.map(item, ValidationRule.class)).collect(Collectors.toList());
	}

	@Override
	public void deleteSingle(Long id) {
		validationRulesRepository.deleteById(id);
	}

	@Override
	public void deleteBulk(List<Long> ids) {
		validationRulesRepository.deleteAllByIdInBatch(ids);
	}
	
	@Transactional
	public void deleteByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory) {
		validationRulesRepository.deleteByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
	}

	public Long countByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory) {
		return validationRulesRepository.countByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
	}

}
