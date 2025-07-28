package com.adp.esi.digitech.ds.config.service;

import java.io.IOException;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import com.adp.esi.digitech.ds.config.entity.DVTSVersioningEntity;
import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.model.DVTSVersioning;
import com.adp.esi.digitech.ds.config.repo.DVTSVersioningRepository;
import com.adp.esi.digitech.ds.config.util.ValidationUtil;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

@Service
public class DVTSVersioningService extends AbstractConfigService<DVTSVersioning> {

	@Autowired
	DVTSVersioningRepository dvtsVersioningRepository;
	
	@Autowired
	ColumnConfigurationService columnConfigurationService;
	
	@Autowired
	ConfigurationImportService configurationImportService;
	
	
	public void revert(Long id, String userrole, String useremail) throws StreamReadException, DatabindException, IOException {
		var optionalEntity = dvtsVersioningRepository.findById(id);
		if(optionalEntity.isPresent()) {
			var data = modelMapper.map(optionalEntity.get(), DVTSVersioning.class);
			data.setUserrole(userrole);
			data.setUseremail(useremail);
			configurationImportService.revertData(data);
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<DVTSVersioning> findAll() {		
		try(var entities = dvtsVersioningRepository.findAllVersions()) {
			return entities.map(entity -> {
    			var item = modelMapper.map(entity, DVTSVersioning.class);
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
	public DVTSVersioning findById(Long id) {
		if(id == null || id.longValue() <= 0)
			throw new ConfigurationException("id should be greater than 0");
		
		var item = dvtsVersioningRepository.findById(id).orElseThrow(()-> new ConfigurationException("No data found for given id = "+ id.longValue()));
		var temp = modelMapper.map(item, DVTSVersioning.class);
		
		if(ValidationUtil.isHavingValue(item.getModifiedBy()))
			temp.setUseremail(item.getModifiedBy());
		else
			temp.setUseremail(item.getCreatedBy());
		
		return temp;
	}

	public List<DVTSVersioning> findBy(String bu, String platform, String dataCategory) {
		var entities = dvtsVersioningRepository.findByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
		
		return entities.stream().map(item -> {
			var temp = modelMapper.map(item, DVTSVersioning.class);
			temp.setDvtsConfigData(null);
			if(ValidationUtil.isHavingValue(item.getModifiedBy()))
				temp.setUseremail(item.getModifiedBy());
			else
				temp.setUseremail(item.getCreatedBy());
			return temp;
		}).collect(Collectors.toList());
	}
	
	@Override
	public DVTSVersioning saveEntity(DVTSVersioning data) {
		DVTSVersioningEntity dvtsVersioningEntity = modelMapper.map(data, DVTSVersioningEntity.class);
		dvtsVersioningEntity.setCreatedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
		dvtsVersioningEntity.setCreatedBy(data.getUseremail());
		var item = dvtsVersioningRepository.save(dvtsVersioningEntity);
		
		var temp = modelMapper.map(item, DVTSVersioning.class);
		
		if(ValidationUtil.isHavingValue(item.getModifiedBy()))
			temp.setUseremail(item.getModifiedBy());
		else
			temp.setUseremail(item.getCreatedBy());
		
		return temp;
		
	}

	@Override
	public List<DVTSVersioning> saveEntities(List<DVTSVersioning> items) {
		if(items == null || items.isEmpty())
			throw new ConfigurationException("Invalid Data, data can't be null or empty");
		var entities = items.parallelStream().map(item -> {
			var entity = modelMapper.map(item, DVTSVersioningEntity.class);
			entity.setCreatedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			entity.setCreatedBy(item.getUseremail());
			return entity;					
		}).collect(Collectors.toList());
		var savedItems = dvtsVersioningRepository.saveAll(entities);
		return savedItems.parallelStream().map(item -> {
			var temp = modelMapper.map(item, DVTSVersioning.class);
			if(ValidationUtil.isHavingValue(item.getModifiedBy()))
				temp.setUseremail(item.getModifiedBy());
			else
				temp.setUseremail(item.getCreatedBy());
			return temp;
		}).collect(Collectors.toList());
		
	}

	@Override
	public DVTSVersioning updateSingle(DVTSVersioning data) {
		Optional<DVTSVersioningEntity> optionalEntity = dvtsVersioningRepository.findById(data.getId());
		if (optionalEntity.isEmpty())
			throw new ConfigurationException("Invalid Data, No data found for given id = "+ data.getId());
		var entity = modelMapper.map(data, DVTSVersioningEntity.class);
		entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
		entity.setModifiedBy(data.getUseremail());
		
		var item = dvtsVersioningRepository.save(entity);
		
		var temp = modelMapper.map(item, DVTSVersioning.class);
		
		if(ValidationUtil.isHavingValue(item.getModifiedBy()))
			temp.setUseremail(item.getModifiedBy());
		else
			temp.setUseremail(item.getCreatedBy());
		
		return temp;
		
	}

	@Override
	public List<DVTSVersioning> updateBulk(List<DVTSVersioning> items) {
		List<DVTSVersioningEntity> entities = items.parallelStream().map(item -> {
			var entity = modelMapper.map(item, DVTSVersioningEntity.class);
			entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			entity.setModifiedBy(item.getUseremail());
			return entity;
		}).collect(Collectors.toList());
		
		List<Long> ids = items.parallelStream().map(rule -> rule.getId()).collect(Collectors.toList());
		
		var tempEntities = dvtsVersioningRepository.findAllById(ids);
		
		var notFoundIds = items.stream().filter(rule -> !tempEntities.stream().anyMatch(entity -> entity.getId().compareTo(rule.getId()) == 0)).map(rule -> rule.getId()).collect(Collectors.toList());
		if (!notFoundIds.isEmpty())
			throw new ConfigurationException("Invalid Data, No data found for given ids = "+ notFoundIds.stream().map(item -> String.valueOf(item)).collect(Collectors.joining(",")));
		
		Map<Long, DVTSVersioningEntity> existing = tempEntities.parallelStream().collect(Collectors.toMap(DVTSVersioningEntity::getId, Function.identity()));
		
		Map<Long, DVTSVersioningEntity> newmap = entities.parallelStream().collect(Collectors.toMap(DVTSVersioningEntity::getId, Function.identity()));
		
		existing.putAll(newmap);
		
		List<DVTSVersioningEntity> entitiesNew = existing.values().stream().collect(Collectors.toList());
		
		var tempItems = dvtsVersioningRepository.saveAll(entitiesNew);
		return tempItems.parallelStream().map(item -> {
			var temp = modelMapper.map(item, DVTSVersioning.class);
			if(ValidationUtil.isHavingValue(item.getModifiedBy()))
				temp.setUseremail(item.getModifiedBy());
			else
				temp.setUseremail(item.getCreatedBy());
			return temp;
		}).collect(Collectors.toList());
	}

	@Override
	public List<DVTSVersioning> patch(List<Map<String, String>> configurations) {
		var isFound = configurations.parallelStream().allMatch(rowMap -> rowMap.containsKey("id") && ValidationUtil.isHavingValue((String)rowMap.get("id")));
		if(!isFound)
			throw new ConfigurationException("id is mandatory and id should not be empty or null");
		
		List<Long> ids = configurations.parallelStream().map(rowMap -> Long.valueOf((String)rowMap.get("id"))).collect(Collectors.toList());
		
		var originalEntities = dvtsVersioningRepository.findAllById(ids);
		
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
						Field field = ReflectionUtils.findField(DVTSVersioningEntity.class, key);
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
		
		var updatedEntities= dvtsVersioningRepository.saveAll(modifiedEntities);
		
		return updatedEntities.parallelStream().map(item -> {
			var temp = modelMapper.map(item, DVTSVersioning.class);
			if(ValidationUtil.isHavingValue(item.getModifiedBy()))
				temp.setUseremail(item.getModifiedBy());
			else
				temp.setUseremail(item.getCreatedBy());
			return temp;
		
		}).collect(Collectors.toList());
	}

	@Override
	public void deleteSingle(Long id) {
		dvtsVersioningRepository.deleteById(id);
		
	}

	@Override
	public void deleteBulk(List<Long> ids) {
		dvtsVersioningRepository.deleteAllByIdInBatch(ids);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {ConfigurationException.class})
	public void deleteBy(String bu, String platform, String dataCategory) {
		dvtsVersioningRepository.deleteByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
	}
	
	public DVTSVersioning createVersion(DVTSVersioning versioning) throws Exception {
		var columnConfigurationResponse = columnConfigurationService.findBy(versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());
		var configData = objectMapper.writeValueAsString(columnConfigurationResponse);
		versioning.setDvtsConfigData(configData);
		return saveEntity(versioning);
	}

}
