package com.adp.esi.digitech.ds.config.service;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import com.adp.esi.digitech.ds.config.entity.ConfigDataEntity;
import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.model.ConfigurationData;
import com.adp.esi.digitech.ds.config.repo.ConfigDataRepository;
import com.adp.esi.digitech.ds.config.repo.specification.ConfigurationDataSpecification;
import com.adp.esi.digitech.ds.config.util.ValidationUtil;

@Service
public class ConfigurationDataService extends AbstractConfigService<ConfigurationData> {

	@Autowired
	ConfigDataRepository configDataRepository;
	
	@Value("#{'${datastudio.config.hidden.sources}'.split(',')}")
	private List<String> sources;

	@Override
	@Transactional(readOnly = true)
	public List<ConfigurationData> findAll() {
		try(var entities = configDataRepository.findAllConfigData()) {
			return entities.map(entity -> {
    			var item = modelMapper.map(entity, ConfigurationData.class);
    			entityManager.detach(entity);
    			return item;
    		}).collect(Collectors.toList());
		}
	}
	
	public List<ConfigurationData> findBy(List<ConfigurationData> configurations) {
		
		return configDataRepository.findAll(ConfigurationDataSpecification.hasData(configurations))
				.parallelStream().map(item -> modelMapper.map(item, ConfigurationData.class))
				.collect(Collectors.toList());
	}
	
	public ConfigurationData findBy(String bu, String platform, String dataCategory) {
		Optional<ConfigDataEntity> optional =  configDataRepository.findByBUPlatformAndDataCategory(bu, platform, dataCategory);
		if(optional.isEmpty())
			throw new ConfigurationException("No data found for given bu = " + bu + ", platform = " + platform +", dataCategory = " + dataCategory);
		return modelMapper.map(optional.get(), ConfigurationData.class);
		//return entities.parallelStream().map(item -> modelMapper.map(item, ColumnRelation.class)).collect(Collectors.toList());
	}
	
	@Override
	public ConfigurationData findById(Long id) {
		if(id == null || id.longValue() <= 0)
			throw new ConfigurationException("id should be greater than 0");
		
		var entity = configDataRepository.findById(id).orElseThrow(()->new ConfigurationException("No data found for given id = "+ id.longValue()));		
		return modelMapper.map(entity, ConfigurationData.class);
	}
	
	@Override
	public ConfigurationData saveEntity(ConfigurationData configurationData) {
		ConfigDataEntity configDataEntity = modelMapper.map(configurationData, ConfigDataEntity.class);
		configDataEntity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
		var temp = configDataRepository.save(configDataEntity);
		return modelMapper.map(temp, ConfigurationData.class);
	}
	
	@Override
	public List<ConfigurationData> saveEntities(List<ConfigurationData> configurationDatas) {	
		if(configurationDatas == null || configurationDatas.isEmpty())
			throw new ConfigurationException("Invalid Data, Configuration Data can't be null or empty");
		var configurationDatasEntities = configurationDatas.parallelStream().map(item -> {
			var entity = modelMapper.map(item, ConfigDataEntity.class);
			entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			return entity;					
		}).collect(Collectors.toList());
		var temp = configDataRepository.saveAll(configurationDatasEntities);
		return temp.parallelStream().map(item -> modelMapper.map(item, ConfigurationData.class)).collect(Collectors.toList());
	}

	@Override
	public ConfigurationData updateSingle(ConfigurationData configurationData) {
		
		Optional<ConfigDataEntity> optionalconfigurationDataEntity = configDataRepository.findById(configurationData.getId());
		if (optionalconfigurationDataEntity.isEmpty())
			throw new ConfigurationException("Invalid Data, No data found for given id = "+ configurationData.getId());
		var entity = modelMapper.map(configurationData, ConfigDataEntity.class);
		entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
		
		var temp = configDataRepository.save(entity);
		return modelMapper.map(temp, ConfigurationData.class);
	}
	
	@Override
	public List<ConfigurationData> updateBulk(List<ConfigurationData> configurationDatas) {
		List<ConfigDataEntity> entities = configurationDatas.parallelStream().map(item -> {
			var entity = modelMapper.map(item, ConfigDataEntity.class);
			entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			return entity;
		}).collect(Collectors.toList());
		
		List<Long> ids = configurationDatas.parallelStream().map(rule -> rule.getId()).collect(Collectors.toList());
		
		var tempEntities = configDataRepository.findAllById(ids);
		
		var notFoundIds = configurationDatas.stream().filter(rule -> !tempEntities.stream().anyMatch(entity -> entity.getId().compareTo(rule.getId()) == 0)).map(rule -> rule.getId()).collect(Collectors.toList());
		if (!notFoundIds.isEmpty())
			throw new ConfigurationException("Invalid Data, No data found for given ids = "+ notFoundIds.stream().map(item -> String.valueOf(item)).collect(Collectors.joining(",")));
		Map<Long, ConfigDataEntity> existing = tempEntities.parallelStream().collect(Collectors.toMap(ConfigDataEntity::getId, Function.identity()));
		Map<Long, ConfigDataEntity> newmap = entities.parallelStream().collect(Collectors.toMap(ConfigDataEntity::getId, Function.identity()));
		existing.putAll(newmap);
		List<ConfigDataEntity> configurationDatasNew = existing.values().stream().collect(Collectors.toList());
		var temp = configDataRepository.saveAll(configurationDatasNew);
		return temp.parallelStream().map(item -> modelMapper.map(item, ConfigurationData.class)).collect(Collectors.toList());
	}
	
	@Override
	public List<ConfigurationData> patch(List<Map<String, String>> configurations) {
		
		var isFound = configurations.parallelStream().allMatch(rowMap -> rowMap.containsKey("id") && ValidationUtil.isHavingValue((String)rowMap.get("id")));
		if(!isFound)
			throw new ConfigurationException("id is mandatory and id should not be empty or null ");
		
		List<Long> ids = configurations.parallelStream().map(rowMap -> Long.valueOf((String)rowMap.get("id"))).collect(Collectors.toList());
		var originalEntities = configDataRepository.findAllById(ids);
		
		if(originalEntities == null || originalEntities.isEmpty())
			throw new ConfigurationException("No data found for given Id's" + ids.toString());
		
		var modifiedEntities = originalEntities.stream().map(entity -> {
			Optional<Map<String, String>> optional = configurations.stream()
																.filter(validation -> entity.getId().compareTo(Long.valueOf((String)validation.get("id"))) == 0)
																.findFirst();
			if(optional.isPresent()) {
				var mapRow = optional.get();
				mapRow.forEach((key, value) -> {
					if(ValidationUtil.isHavingValue(key) && !key.equalsIgnoreCase("id")) {
						Field field = ReflectionUtils.findField(ConfigDataEntity.class, key);
						field.setAccessible(true);
						ReflectionUtils.setField(field, entity, value);
					}
				});
				entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			}
			
			return entity;
		}).collect(Collectors.toList());
		
		var updatedEntities= configDataRepository.saveAll(modifiedEntities);
		return updatedEntities.parallelStream().map(item -> modelMapper.map(item, ConfigurationData.class)).collect(Collectors.toList());
	}

	@Override
	public void deleteSingle(Long id) {
		configDataRepository.deleteById(id);
	}

	@Override
	public void deleteBulk(List<Long> ids) {
		configDataRepository.deleteAllByIdInBatch(ids);
	}
	
	@Transactional
	public void deleteByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory) {
		configDataRepository.deleteByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
	}

	public Long countByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory) {
		return configDataRepository.countByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
	}

	public Map<String, Map<String, List<String>>> getBUPlatformAndDataCategoryBasicInfo() {
		var entities = configDataRepository.findBUPlatformAndDataCategoryAndSourceBasicInfo(sources);
		
		return entities.stream().collect(Collectors.groupingBy(buObj -> (String)buObj[0], LinkedHashMap::new, Collectors.groupingBy(platformObj -> (String)platformObj[1], LinkedHashMap::new,
				Collectors.mapping(dcObj -> (String)dcObj[2], Collectors.toList()))));
	}
	public ConfigurationData findByBUPlatformAndDataCategory(String bu, String platform, String dataCategory) {
		Optional<ConfigDataEntity> optional =  configDataRepository.findByBUPlatformAndDataCategory(bu, platform, dataCategory);
		if(optional.isEmpty())
			return null;
		return modelMapper.map(optional.get(), ConfigurationData.class);
	}
}
