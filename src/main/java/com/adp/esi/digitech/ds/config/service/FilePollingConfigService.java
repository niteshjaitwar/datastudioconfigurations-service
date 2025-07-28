package com.adp.esi.digitech.ds.config.service;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import com.adp.esi.digitech.ds.config.entity.FilePollingConfigurationEntity;
import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.model.FilePollingConfig;
import com.adp.esi.digitech.ds.config.repo.FilePollingConfigRepository;
import com.adp.esi.digitech.ds.config.util.ValidationUtil;

@Service
public class FilePollingConfigService extends AbstractConfigService<FilePollingConfig> {
	
	@Autowired
	FilePollingConfigRepository filePollingConfigRepository;

	@Override
	@Transactional(readOnly = true)
	public List<FilePollingConfig> findAll() {
		try(var entities = filePollingConfigRepository.findAllFilePollingConfigs()) {
			return entities.map(entity -> {
    			var item = modelMapper.map(entity, FilePollingConfig.class);
    			if(ValidationUtil.isHavingValue(entity.getModifiedBy()))
    				item.setUseremail(entity.getModifiedBy());
    			else
    				item.setUseremail(entity.getCreatedBy());
    			entityManager.detach(entity);
    			return item;
    		}).collect(Collectors.toList());
		}
	}
	
	@Override
	public FilePollingConfig findById(Long id) {
		if(id == null || id.longValue() <= 0)
			throw new ConfigurationException("id should be greater than 0");
		var item = filePollingConfigRepository.findById(id).orElseThrow(() -> new ConfigurationException("No data found for given id = "+ id.longValue()));
		var temp = modelMapper.map(item, FilePollingConfig.class);
		
		if(ValidationUtil.isHavingValue(item.getModifiedBy()))
			temp.setUseremail(item.getModifiedBy());
		else
			temp.setUseremail(item.getCreatedBy());
		
		return temp;		
	}
	
	public FilePollingConfig findByBu(String bu) {
		var entity = filePollingConfigRepository.findByBu(bu);
		var temp = modelMapper.map(entity, FilePollingConfig.class);
		if(ValidationUtil.isHavingValue(entity.getModifiedBy()))
			temp.setUseremail(entity.getModifiedBy());
		else
			temp.setUseremail(entity.getCreatedBy());
		return temp;
	}
	
	@Override
	public FilePollingConfig saveEntity(FilePollingConfig columnRelation) {
		FilePollingConfigurationEntity FilePollingConfigurationEntity = modelMapper.map(columnRelation, FilePollingConfigurationEntity.class);
		FilePollingConfigurationEntity.setCreatedBy(columnRelation.getUseremail());
		FilePollingConfigurationEntity.setCreatedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
		
		var item = filePollingConfigRepository.save(FilePollingConfigurationEntity);
		
		var temp = modelMapper.map(item, FilePollingConfig.class);
		
		if(ValidationUtil.isHavingValue(item.getModifiedBy()))
			temp.setUseremail(item.getModifiedBy());
		else
			temp.setUseremail(item.getCreatedBy());
		
		return temp;
	}
	

	@Override
	public List<FilePollingConfig> saveEntities(List<FilePollingConfig> filePollingConfigs) {	
		if(filePollingConfigs == null || filePollingConfigs.isEmpty())
			throw new ConfigurationException("Invalid Data, File Polling Config can't be null or empty");
		
		var filePollingConfigsEntities = filePollingConfigs.parallelStream().map(item -> {
			var entity = modelMapper.map(item, FilePollingConfigurationEntity.class);
			var time = Timestamp.valueOf(simpleDateFormat.format(new Date()));
			entity.setCreatedDateTime(time);
			entity.setCreatedBy(item.getUseremail());
			entity.setModifiedDateTime(time);
			entity.setModifiedBy(item.getUseremail());
			return entity;					
		}).collect(Collectors.toList());
		var items = filePollingConfigRepository.saveAll(filePollingConfigsEntities);
		return items.parallelStream().map(item -> {
			var temp = modelMapper.map(item, FilePollingConfig.class);
			if(ValidationUtil.isHavingValue(item.getModifiedBy()))
				temp.setUseremail(item.getModifiedBy());
			else
				temp.setUseremail(item.getCreatedBy());
			return temp;
		}).collect(Collectors.toList());
	}	

	@Override
	public FilePollingConfig updateSingle(FilePollingConfig filePollingConfig) {
		
		Optional<FilePollingConfigurationEntity> optionalfilePollingConfigEntity = filePollingConfigRepository.findById(filePollingConfig.getId());
		if (optionalfilePollingConfigEntity.isEmpty())
			throw new ConfigurationException("Invalid Data, No data found for given id = "+ filePollingConfig.getId());
		var entity = modelMapper.map(filePollingConfig, FilePollingConfigurationEntity.class);
		entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
		entity.setModifiedBy(filePollingConfig.getUseremail());
		
		var item = filePollingConfigRepository.save(entity);
		var temp = modelMapper.map(item, FilePollingConfig.class);
		
		if(ValidationUtil.isHavingValue(item.getModifiedBy()))
			temp.setUseremail(item.getModifiedBy());
		else
			temp.setUseremail(item.getCreatedBy());
		
		return temp;
	}
	
	@Override
	public List<FilePollingConfig> updateBulk(List<FilePollingConfig> filePollingConfigs) {
		var entities = filePollingConfigs.parallelStream().map(item -> {
			var entity = modelMapper.map(item, FilePollingConfigurationEntity.class);
			entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			entity.setModifiedBy(item.getUseremail());
			return entity;
		}).collect(Collectors.toList());
		
		List<Long> ids = filePollingConfigs.parallelStream().map(rule -> rule.getId()).collect(Collectors.toList());
		
		var tempEntities = filePollingConfigRepository.findAllById(ids);
		
		var notFoundIds = filePollingConfigs.stream().filter(rule -> !tempEntities.stream().anyMatch(entity -> entity.getId().compareTo(rule.getId()) == 0)).map(rule -> rule.getId()).collect(Collectors.toList());
		if (!notFoundIds.isEmpty())
			throw new ConfigurationException("Invalid Data, No data found for given ids = "+ notFoundIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
		Map<Long, FilePollingConfigurationEntity> existing = tempEntities.parallelStream().collect(Collectors.toMap(FilePollingConfigurationEntity::getId, Function.identity()));
		Map<Long, FilePollingConfigurationEntity> newmap = entities.parallelStream().collect(Collectors.toMap(FilePollingConfigurationEntity::getId, Function.identity()));
		existing.putAll(newmap);
		List<FilePollingConfigurationEntity> columnRelationsNew = existing.values().stream().collect(Collectors.toList());
		var items = filePollingConfigRepository.saveAll(columnRelationsNew);
		
		return items.parallelStream().map(item -> {
			var temp = modelMapper.map(item, FilePollingConfig.class);
			if(ValidationUtil.isHavingValue(item.getModifiedBy()))
				temp.setUseremail(item.getModifiedBy());
			else
				temp.setUseremail(item.getCreatedBy());
			return temp;
		}).collect(Collectors.toList());
	}
	
	@Override
	public List<FilePollingConfig> patch(List<Map<String, String>> filePollingConfigs) {
		
		var isFound = filePollingConfigs.parallelStream().allMatch(rowMap -> rowMap.containsKey("id") && ValidationUtil.isHavingValue((String)rowMap.get("id")));
		if(!isFound)
			throw new ConfigurationException("id is mandatory and id should not be empty or null ");
		
		List<Long> ids = filePollingConfigs.parallelStream().map(rowMap -> Long.valueOf((String)rowMap.get("id"))).collect(Collectors.toList());
		var originalEntities = filePollingConfigRepository.findAllById(ids);
		
		if(originalEntities == null || originalEntities.isEmpty())
			throw new ConfigurationException("No data found for given Id's" + ids.toString());
		
		var modifiedEntities = originalEntities.stream().map(entity -> {
			Optional<Map<String, String>> optional = filePollingConfigs.stream()
																.filter(validation -> entity.getId().compareTo(Long.valueOf((String)validation.get("id"))) == 0)
																.findFirst();
			if(optional.isPresent()) {
				var mapRow = optional.get();
				mapRow.forEach((key, value) -> {
					if(ValidationUtil.isHavingValue(key) && !key.equalsIgnoreCase("id")  && !key.equalsIgnoreCase("useremail")) {
						Field field = ReflectionUtils.findField(FilePollingConfigurationEntity.class, key);
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
		
		var updatedEntities= filePollingConfigRepository.saveAll(modifiedEntities);
		return updatedEntities.parallelStream().map(item -> {
			var temp = modelMapper.map(item, FilePollingConfig.class);
			if(ValidationUtil.isHavingValue(item.getModifiedBy()))
				temp.setUseremail(item.getModifiedBy());
			else
				temp.setUseremail(item.getCreatedBy());
			return temp;
		
		}).collect(Collectors.toList());
	}
	
	@Override
	public void deleteSingle(Long id) {
		filePollingConfigRepository.deleteById(id);
	}

	@Override
	public void deleteBulk(List<Long> ids) {
		filePollingConfigRepository.deleteAllByIdInBatch(ids);
	}

}
