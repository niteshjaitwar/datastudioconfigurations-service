package com.adp.esi.digitech.ds.config.service;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import com.adp.esi.digitech.ds.config.entity.ColumnRelationEntity;
import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.model.ColumnRelation;
import com.adp.esi.digitech.ds.config.repo.ColumnRelationRepository;
import com.adp.esi.digitech.ds.config.util.ValidationUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ColumnRelationService extends AbstractConfigService<ColumnRelation> {
	
	@Autowired
	ColumnRelationRepository columnRelationRepository;
	
	@Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
	int batchSize;

	@Override
	@Transactional(readOnly = true)
	public List<ColumnRelation> findAll() {
		try(var entities = columnRelationRepository.findAllColumnRelations()) {
			return entities.map(entity -> {
    			var item = modelMapper.map(entity, ColumnRelation.class);
    			if(ValidationUtil.isHavingValue(entity.getModifiedBy()))
    				item.setUseremail(entity.getModifiedBy());
    			else
    				item.setUseremail(entity.getCreatedBy());
    			entityManager.detach(entity);
    			return item;
    		}).collect(Collectors.toList());
		}
	}
	
	public Map<UUID, ColumnRelation> findBy(String bu, String platform, String dataCategory) {
		/*
		 * var entities = find(bu, platform, dataCategory); return
		 * entities.stream().map(item -> { var temp = modelMapper.map(item,
		 * ColumnRelation.class); if(ValidationUtil.isHavingValue(item.getModifiedBy()))
		 * temp.setUseremail(item.getModifiedBy()); else
		 * temp.setUseremail(item.getCreatedBy()); return temp;
		 * }).collect(Collectors.groupingBy(ColumnRelation::getSourceKey));
		 *///return entities.parallelStream().map(item -> modelMapper.map(item, ColumnRelation.class)).collect(Collectors.toList());
		return find(bu, platform, dataCategory).stream().collect(Collectors.toMap(item -> UUID.fromString(item.getUuid()) , Function.identity()));
	}
	
	public List<ColumnRelation> find(String bu, String platform, String dataCategory) {
		var entities = columnRelationRepository.findByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
		return entities.parallelStream().map(result -> {			
			return ColumnRelation.builder()
					.id(((Number) result[0]).longValue())
					.bu((String)result[1])
					.platform((String)result[2])
					.dataCategory((String)result[3])
					.sourceKey((String)result[4])
					.columnName((String)result[5])
					.position(Objects.nonNull(result[6])? ((Number) result[6]).longValue(): null)
					.aliasName((String)result[7])
					.uuid((String)result[8])					
					.required(Objects.nonNull(result[9])? String.valueOf(result[9]):null)
					.columnRequiredInErrorFile((String)result[10])
					.useremail((String)result[11])
					.userrole((String)result[12])
					.dataExclusionRules((String)result[13])
					.dataType((String)result[14])
					.format((String)result[15])
					.path((String)result[16])
					.build();
		}).collect(Collectors.toList());
	}
	
	public List<ColumnRelation> findByList(String bu, String platform, String dataCategory) {
		//var entities = columnRelationRepository.findByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
		//return entities.parallelStream().map(item -> modelMapper.map(item, ColumnRelation.class)).collect(Collectors.toList());
		return find(bu, platform, dataCategory);
	}
	
	public List<Long> findIdsListBy(String bu, String platform, String dataCategory) {
		return columnRelationRepository.findIdByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
	}

	public Map<String,List<ColumnRelation>> findBy(String bu, String platform, String dataCategory, String sourceKey) {
		var entities = columnRelationRepository.findByBuAndPlatformAndDataCategoryAndSourceKeyLike(bu, platform, dataCategory, sourceKey + "%");
		return entities.parallelStream().filter(item -> !"DELETED".equalsIgnoreCase(item.getStatus())).map(item -> {
			var temp = modelMapper.map(item, ColumnRelation.class);
			if(ValidationUtil.isHavingValue(item.getModifiedBy()))
				temp.setUseremail(item.getModifiedBy());
			else
				temp.setUseremail(item.getCreatedBy());
			return temp;
		}).collect(Collectors.groupingBy(ColumnRelation::getSourceKey));
	}
	
	@Override
	public ColumnRelation findById(Long id) {
		if(id == null || id.longValue() <= 0)
			throw new ConfigurationException("id should be greater than 0");
		
		var item = columnRelationRepository.findById(id).orElseThrow(() -> new ConfigurationException("No data found for given id = "+ id.longValue()));
		var temp = modelMapper.map(item, ColumnRelation.class);
		
		if(ValidationUtil.isHavingValue(item.getModifiedBy()))
			temp.setUseremail(item.getModifiedBy());
		else
			temp.setUseremail(item.getCreatedBy());
		
		return temp;		
	}
	
	@Override
	public ColumnRelation saveEntity(ColumnRelation columnRelation) {
		if(!ValidationUtil.isHavingValue(columnRelation.getUuid()))
			columnRelation.setUuid(UUID.randomUUID().toString());
		ColumnRelationEntity columnRelationEntity = modelMapper.map(columnRelation, ColumnRelationEntity.class);
		columnRelationEntity.setCreatedBy(columnRelation.getUseremail());
		columnRelationEntity.setCreatedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
		
		var item = columnRelationRepository.save(columnRelationEntity);
		
		var temp = modelMapper.map(item, ColumnRelation.class);
		
		if(ValidationUtil.isHavingValue(item.getModifiedBy()))
			temp.setUseremail(item.getModifiedBy());
		else
			temp.setUseremail(item.getCreatedBy());
		
		return temp;
	}

	@Override
	public List<ColumnRelation> saveEntities(List<ColumnRelation> columnRelations) {	
		if(columnRelations == null || columnRelations.isEmpty())
			throw new ConfigurationException("Invalid Data, Column Relations can't be null or empty");
		
		var columnRelationsEntities = columnRelations.parallelStream().map(item -> {
			if(!ValidationUtil.isHavingValue(item.getUuid()))
				item.setUuid(UUID.randomUUID().toString());
			var entity = modelMapper.map(item, ColumnRelationEntity.class);
			var time = Timestamp.valueOf(simpleDateFormat.format(new Date()));
			entity.setCreatedDateTime(time);
			entity.setCreatedBy(item.getUseremail());
			entity.setModifiedDateTime(time);
			entity.setModifiedBy(item.getUseremail());
			return entity;					
		}).collect(Collectors.toList());
		log.info("ColumnRelationService - saveEntities() comlpeted creating column relation entities size = {}", columnRelationsEntities.size());
		//var batchSize = 50;
		var columnRelationsLength = columnRelationsEntities.size();
		List<ColumnRelationEntity> items = new ArrayList<>();
		for (int i = 0; i < columnRelationsLength ; i = i + batchSize) {
		    if( i+ batchSize > columnRelationsLength){
		       var tempColumnRelationsEntities = columnRelationsEntities.subList(i, columnRelationsLength);
		       items.addAll(columnRelationRepository.saveAll(tempColumnRelationsEntities));
		       break;
		    }
		    var tempColumnRelationsEntities = columnRelationsEntities.subList(i, i + batchSize);
		    items.addAll(columnRelationRepository.saveAll(tempColumnRelationsEntities));
		    log.info("ColumnRelationService - saveEntities()  Completed saving partial tempColumnRelationsEntities, i = {}", i + batchSize);
		}
		log.info("ColumnRelationService - saveEntities() comlpeted saving column relation entities size = {}", items.size());
		//var items = columnRelationRepository.saveAll(columnRelationsEntities);
		return items.parallelStream().map(item -> {
			var temp = modelMapper.map(item, ColumnRelation.class);
			if(ValidationUtil.isHavingValue(item.getModifiedBy()))
				temp.setUseremail(item.getModifiedBy());
			else
				temp.setUseremail(item.getCreatedBy());
			return temp;
		}).collect(Collectors.toList());
	}	

	@Override
	public ColumnRelation updateSingle(ColumnRelation columnRelation) {
		
		Optional<ColumnRelationEntity> optionalColumnRelationEntity = columnRelationRepository.findById(columnRelation.getId());
		if (optionalColumnRelationEntity.isEmpty())
			throw new ConfigurationException("Invalid Data, No data found for given id = "+ columnRelation.getId());
		var entity = modelMapper.map(columnRelation, ColumnRelationEntity.class);
		entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
		entity.setModifiedBy(columnRelation.getUseremail());
		
		
		var item = columnRelationRepository.save(entity);
		var temp = modelMapper.map(item, ColumnRelation.class);
		
		if(ValidationUtil.isHavingValue(item.getModifiedBy()))
			temp.setUseremail(item.getModifiedBy());
		else
			temp.setUseremail(item.getCreatedBy());
		
		return temp;
	}

	@Override
	public List<ColumnRelation> updateBulk(List<ColumnRelation> columnRelations) {
		List<ColumnRelationEntity> entities = columnRelations.parallelStream().map(item -> {
			var entity = modelMapper.map(item, ColumnRelationEntity.class);
			entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			entity.setModifiedBy(item.getUseremail());
			return entity;
		}).collect(Collectors.toList());
		
		List<Long> ids = columnRelations.parallelStream().map(rule -> rule.getId()).collect(Collectors.toList());
		
		var tempEntities = columnRelationRepository.findAllById(ids);
		
		var notFoundIds = columnRelations.stream().filter(rule -> !tempEntities.stream().anyMatch(entity -> entity.getId().compareTo(rule.getId()) == 0)).map(rule -> rule.getId()).collect(Collectors.toList());
		if (!notFoundIds.isEmpty())
			throw new ConfigurationException("Invalid Data, No data found for given ids = "+ notFoundIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
		Map<Long, ColumnRelationEntity> existing = tempEntities.parallelStream().collect(Collectors.toMap(ColumnRelationEntity::getId, Function.identity()));
		Map<Long, ColumnRelationEntity> newmap = entities.parallelStream().collect(Collectors.toMap(ColumnRelationEntity::getId, Function.identity()));
		existing.putAll(newmap);
		List<ColumnRelationEntity> columnRelationsNew = existing.values().stream().collect(Collectors.toList());
		var items = columnRelationRepository.saveAll(columnRelationsNew);
		
		return items.parallelStream().map(item -> {
			var temp = modelMapper.map(item, ColumnRelation.class);
			if(ValidationUtil.isHavingValue(item.getModifiedBy()))
				temp.setUseremail(item.getModifiedBy());
			else
				temp.setUseremail(item.getCreatedBy());
			return temp;
		}).collect(Collectors.toList());
	}


	@Override
	public List<ColumnRelation> patch(List<Map<String, String>> columnRelations) {
		
		var isFound = columnRelations.parallelStream().allMatch(rowMap -> rowMap.containsKey("id") && ValidationUtil.isHavingValue((String)rowMap.get("id")));
		if(!isFound)
			throw new ConfigurationException("id is mandatory and id should not be empty or null ");
		
		List<Long> ids = columnRelations.parallelStream().map(rowMap -> Long.valueOf((String)rowMap.get("id"))).collect(Collectors.toList());
		var originalEntities = columnRelationRepository.findAllById(ids);
		
		if(originalEntities == null || originalEntities.isEmpty())
			throw new ConfigurationException("No data found for given Id's" + ids.toString());
		
		var modifiedEntities = originalEntities.stream().map(entity -> {
			Optional<Map<String, String>> optional = columnRelations.stream()
																.filter(validation -> entity.getId().compareTo(Long.valueOf((String)validation.get("id"))) == 0)
																.findFirst();
			if(optional.isPresent()) {
				var mapRow = optional.get();
				mapRow.forEach((key, value) -> {
					if(ValidationUtil.isHavingValue(key) && !key.equalsIgnoreCase("id")  && !key.equalsIgnoreCase("useremail") && !key.equalsIgnoreCase("position")) {
						Field field = ReflectionUtils.findField(ColumnRelationEntity.class, key);
						field.setAccessible(true);
						ReflectionUtils.setField(field, entity, value);
					}
					if(key.equalsIgnoreCase("useremail"))
						entity.setModifiedBy(value);
					else if(key.equalsIgnoreCase("position"))
						entity.setPosition(Long.valueOf(value));
				});
				
				entity.setModifiedDateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
			}
			
			return entity;
		}).collect(Collectors.toList());
		
		var updatedEntities= columnRelationRepository.saveAll(modifiedEntities);
		return updatedEntities.parallelStream().map(item -> {
			var temp = modelMapper.map(item, ColumnRelation.class);
			if(ValidationUtil.isHavingValue(item.getModifiedBy()))
				temp.setUseremail(item.getModifiedBy());
			else
				temp.setUseremail(item.getCreatedBy());
			return temp;
		
		}).collect(Collectors.toList());
	}

	@Override
	public void deleteSingle(Long id) {
		columnRelationRepository.deleteById(id);
	}

	@Override
	public void deleteBulk(List<Long> ids) {
		columnRelationRepository.deleteAllByIdInBatch(ids);
	}
	
	@Transactional
	public void deleteByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory) {
		columnRelationRepository.deleteByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
	}
	
	public Long countByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory) {
		return columnRelationRepository.countByBuAndPlatformAndDataCategory(bu, platform, dataCategory);
	}

}
