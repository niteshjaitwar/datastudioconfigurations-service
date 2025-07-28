package com.adp.esi.digitech.ds.config.service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;

//import com.adp.digitech.fileprocessing.v2.validator.service.ExcelMetadataValidatorService;
import com.adp.esi.digitech.ds.config.entity.LOVTypeEntity;
import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.model.LovMetadata;
import com.adp.esi.digitech.ds.config.model.Pagination;
import com.adp.esi.digitech.ds.config.model.Values;
import com.adp.esi.digitech.ds.config.repo.LOVTypeRepository;
import com.adp.esi.digitech.ds.config.repo.specification.LovMetadataSpecification;
import com.adp.esi.digitech.ds.config.util.ValidationUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LOVMetadataService extends AbstractConfigService<LovMetadata> {

	@Autowired
	LOVTypeRepository lovTypeRepository;
	
	@Autowired
	DataFormatter dataFormatter;
	
	@Autowired
	ComplexLOVMetadataService complexLOVMetadataService;
	
	@Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
	int batchSize;
	
	@Value("${pagination.lov.fetch.size}")
	int pageSize;
	
	@Value("${pagination.lov.fetch.max.size}")
	int maxPageSize;
	
	@Value("${datastudio.excel.type}")
	private String excelFileType;
	
	public static final String SIMPLE = "simple";
	
    @Override
    @Transactional(readOnly = true)
	public List<LovMetadata> findAll() {
    	try(var lovs = lovTypeRepository.findAllLov()) {
			return lovs.map(lov -> {
    			var item = modelMapper.map(lov, LovMetadata.class);
    			entityManager.detach(lov);
    			return item;
    		}).collect(Collectors.toList());
		}
		
	}
    
    @Transactional(readOnly = true)
    public Properties findAllProperties(String type) {	
    	Properties props = new Properties();
    	if(!ValidationUtil.isHavingValue(type) || type.isEmpty())
			throw new ConfigurationException("type should not be null or empty");
    	
    	try(var lovs = lovTypeRepository.findByLovType(type)) {
			lovs.forEach(lov -> {
				props.setProperty(lov.getLovName(), lov.getLovValue());
				entityManager.detach(lov);
			});
			return props;
		}	
    			
	}
    
    @Transactional(readOnly = true)
    public List<LovMetadata> findAll(String type) {    	
    	if(!ValidationUtil.isHavingValue(type) || type.isEmpty())
			throw new ConfigurationException("type should not be null or empty");
    	
    	try(var lovs = lovTypeRepository.findByLovType(type)) {
    		return lovs.map(lov -> {
    			var item = modelMapper.map(lov, LovMetadata.class);
    			entityManager.detach(lov);
    			return item;
    		}).collect(Collectors.toList());
    	}
	}

	@Override
	public LovMetadata findById(Long id) {
		if(id == null || id.longValue() <= 0)
			throw new ConfigurationException("id should be greater than 0");
		
		var entity = lovTypeRepository.findById(id).orElseThrow(() -> new ConfigurationException("No data found for given id = "+ id.longValue()));
		
		return modelMapper.map(entity, LovMetadata.class);
	}
	
	public Map<String, List<LovMetadata>> findByLovTypeIn(List<String>  lovTypes) {
		if(lovTypes == null || lovTypes.isEmpty())
			throw new ConfigurationException("lovTypes should not be null or empty");
		var entities = lovTypeRepository.findByLovTypeIn(lovTypes);
		if(entities == null || entities.isEmpty())
			throw new ConfigurationException("No data found for given lovTypes = "+ String.join(",", lovTypes));
		return entities.parallelStream().map(item -> modelMapper.map(item, LovMetadata.class)).collect(Collectors.groupingBy(LovMetadata::getLovType));
	}
	
	@Override
	public LovMetadata saveEntity(LovMetadata lovMetadata) {
		LOVTypeEntity configDataEntity = modelMapper.map(lovMetadata, LOVTypeEntity.class);
		configDataEntity = getTrimmedLovData(configDataEntity);
		var temp = lovTypeRepository.save(configDataEntity);
		return modelMapper.map(temp, LovMetadata.class);
	}
	
	@Override
	public List<LovMetadata> saveEntities(List<LovMetadata> lovMetadatas) {	
		if(lovMetadatas == null || lovMetadatas.isEmpty())
			throw new ConfigurationException("Invalid Data, LOV Data can't be null or empty");
		var lovDatasEntities = lovMetadatas.parallelStream().map(item -> {
			LOVTypeEntity lovTypeEntity = modelMapper.map(item, LOVTypeEntity.class);
			lovTypeEntity = getTrimmedLovData(lovTypeEntity);
			return lovTypeEntity;
					}).collect(Collectors.toList());
		var temp = lovTypeRepository.saveAll(lovDatasEntities);
		return temp.parallelStream().map(item -> modelMapper.map(item, LovMetadata.class)).collect(Collectors.toList());
	}
	
	@Override
	public LovMetadata updateSingle(LovMetadata lovMetadata) {
		
		Optional<LOVTypeEntity> optionalLovDataEntity = lovTypeRepository.findById(lovMetadata.getId());
		if (optionalLovDataEntity.isEmpty())
			throw new ConfigurationException("Invalid Data, No data found for given id = "+ lovMetadata.getId());
		var entity = modelMapper.map(lovMetadata, LOVTypeEntity.class);	
		entity = getTrimmedLovData(entity);
		var temp = lovTypeRepository.save(entity);
		return modelMapper.map(temp, LovMetadata.class);
	}
	
	@Override
	public List<LovMetadata> updateBulk(List<LovMetadata> lovMetadatas) {
		List<LOVTypeEntity> entities = lovMetadatas.parallelStream().map(item -> modelMapper.map(item, LOVTypeEntity.class)).collect(Collectors.toList());
		
		List<Long> ids = lovMetadatas.parallelStream().map(lov -> lov.getId()).collect(Collectors.toList());
		
		var tempEntities = lovTypeRepository.findAllById(ids);
		
		var notFoundIds = lovMetadatas.stream().filter(lov -> !tempEntities.stream().anyMatch(entity -> entity.getId().compareTo(lov.getId()) == 0)).map(rule -> rule.getId()).collect(Collectors.toList());
		if (!notFoundIds.isEmpty())
			throw new ConfigurationException("Invalid Data, No data found for given ids = "+ notFoundIds.stream().map(item -> String.valueOf(item)).collect(Collectors.joining(",")));
		Map<Long, LOVTypeEntity> existing = tempEntities.parallelStream().collect(Collectors.toMap(LOVTypeEntity::getId, Function.identity()));
		Map<Long, LOVTypeEntity> newmap = entities.parallelStream().collect(Collectors.toMap(LOVTypeEntity::getId, Function.identity()));
		existing.putAll(newmap);
		List<LOVTypeEntity> lovDatasNew = existing.values().stream().collect(Collectors.toList());
		var trimmedLovData = lovDatasNew.parallelStream().map(lovDataNew -> {
			lovDataNew = getTrimmedLovData(lovDataNew);
			return lovDataNew;
			}).collect(Collectors.toList());
		var temp = lovTypeRepository.saveAll(trimmedLovData);
		return temp.parallelStream().map(item -> modelMapper.map(item, LovMetadata.class)).collect(Collectors.toList());
	}
	
	@Override
	public List<LovMetadata> patch(List<Map<String, String>> lovs) {
		
		var isFound = lovs.parallelStream().allMatch(rowMap -> rowMap.containsKey("id") && ValidationUtil.isHavingValue((String)rowMap.get("id")));
		if(!isFound)
			throw new ConfigurationException("id is mandatory and id should not be empty or null ");
		
		List<Long> ids = lovs.parallelStream().map(rowMap -> Long.valueOf((String)rowMap.get("id"))).collect(Collectors.toList());
		var originalEntities = lovTypeRepository.findAllById(ids);
		
		if(originalEntities == null || originalEntities.isEmpty())
			throw new ConfigurationException("No data found for given Id's" + ids.toString());
		
		var modifiedEntities = originalEntities.stream().map(entity -> {
			Optional<Map<String, String>> optional = lovs.stream()
																.filter(validation -> entity.getId().compareTo(Long.valueOf((String)validation.get("id"))) == 0)
																.findFirst();
			if(optional.isPresent()) {
				var mapRow = optional.get();
				mapRow.forEach((key, value) -> {
					if(ValidationUtil.isHavingValue(key) && !key.equalsIgnoreCase("id")) {
						Field field = ReflectionUtils.findField(LOVTypeEntity.class, key);
						field.setAccessible(true);
						ReflectionUtils.setField(field, entity, value);
					}
				});
			}
			var trimmedLovEntity = getTrimmedLovData(entity);
			return trimmedLovEntity;
		}).collect(Collectors.toList());
		
		var updatedEntities= lovTypeRepository.saveAll(modifiedEntities);
		return updatedEntities.parallelStream().map(item -> modelMapper.map(item, LovMetadata.class)).collect(Collectors.toList());
	}

	@Override
	public void deleteSingle(Long id) {
		lovTypeRepository.deleteById(id);
	}

	@Override
	public void deleteBulk(List<Long> ids) {
		lovTypeRepository.deleteAllByIdInBatch(ids);
	}

	public List<LovMetadata> saveEntities(MultipartFile lovFile) throws IOException {	
		if (lovFile == null)
			throw new ConfigurationException("Invalid Data, File can't be null");
		
		if (!excelFileType.equalsIgnoreCase(lovFile.getContentType()))
			throw new ConfigurationException("Invalid File Format, Only Excel File is allowed");
		
		Workbook workbook  = new XSSFWorkbook(lovFile.getInputStream());
		
		List<LOVTypeEntity> lovlist = null;
		try {
			lovlist = IntStream.range(0, workbook.getNumberOfSheets()).parallel().mapToObj(index -> {
				List<LOVTypeEntity> lovTypeEntities = new ArrayList<LOVTypeEntity>();
				org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(index);
				Iterator<Row> rowIterator = sheet.iterator();
				Row row = rowIterator.next();
				if(!(row.getPhysicalNumberOfCells() == 3
						&& row.getCell(0).getStringCellValue().equalsIgnoreCase("LOV_TYPE")
						&& row.getCell(1).getStringCellValue().equalsIgnoreCase("LOV_NAME")
						&& row.getCell(2).getStringCellValue().equalsIgnoreCase("LOV_VALUE"))) {					
					throw new ConfigurationException("Missing or invalid headers .The LOV header size is 3 and headers are LOV_TYPE,LOV_NAME,LOV_VALUE");
				}			
				
				while (rowIterator.hasNext()) {
					LOVTypeEntity lov = new LOVTypeEntity();
					Row nextRow = rowIterator.next();
					lov.setLovType(dataFormatter.formatCellValue(nextRow.getCell(0)));
					lov.setLovName(dataFormatter.formatCellValue(nextRow.getCell(1)));
					lov.setLovValue(dataFormatter.formatCellValue(nextRow.getCell(2)));
					lov=getTrimmedLovData(lov);
					lovTypeEntities.add(lov);
				}
				
				return lovTypeEntities;
			}).collect(Collectors.toList()).stream().flatMap(List::stream).collect(Collectors.toList());;
		} finally {
			if(workbook != null)
				workbook.close();
		}
		if(lovlist == null || lovlist.isEmpty())
			throw new ConfigurationException("No LOV Data Found in the document");
		
		log.info("LOVMetadataService - saveEntities()  Completed Constructing lovlist, size = {}", lovlist.size());
		//var batchSize = 50;
		var lovlistLength = lovlist.size();
		List<LOVTypeEntity> temp = new ArrayList<>();
		for (int i = 0; i < lovlistLength ; i = i + batchSize) {
		    if( i+ batchSize > lovlistLength){
		       var tempLOVEntities = lovlist.subList(i, lovlistLength);
		       temp.addAll(lovTypeRepository.saveAll(tempLOVEntities));
		       break;
		    }
		    var tempColumnRelationsEntities = lovlist.subList(i, i + batchSize);
		    temp.addAll(lovTypeRepository.saveAll(tempColumnRelationsEntities));
		    log.info("LOVMetadataService - saveEntities()  Completed saving partial lovlist, i = {}", i + batchSize);
		}
		log.info("LOVMetadataService - saveEntities()  Completed saving lovlist, size = {}", temp.size());
		//var temp = lovTypeRepository.saveAll(lovlist);
		return temp.parallelStream().map(item -> modelMapper.map(item, LovMetadata.class)).collect(Collectors.toList());
	}
	

	public Map<String, Pagination> getAllSimpleLovTypeBasicDetails(){
		var allLovTypeDetails = lovTypeRepository.findAllLovTypeDetails();
		Function<Integer, Pagination> objFun = cnt -> {
			var size = cnt/pageSize;
			var balance = cnt%pageSize;
			
			if(balance > 0) size++;							
			return Pagination.builder().count(cnt).pages(size).type(LOVMetadataService.SIMPLE).build();
		};
		return allLovTypeDetails.stream().collect(Collectors.toMap(lovTypeDetailsArray -> String.valueOf(lovTypeDetailsArray[0]), 
				lovTypeDetailsArray -> objFun.apply(((BigDecimal)lovTypeDetailsArray[1]).intValue()), (oldValue, newValue)-> oldValue, LinkedHashMap::new));
		
		
	}
	
	public Map<String, Pagination> getAllLovTypeBasicDetails() {
		Map<String, Pagination> result = new LinkedHashMap<>();	
		result.putAll(complexLOVMetadataService.getAllComplexLovTypeBasicDetails());		
		result.putAll(getAllSimpleLovTypeBasicDetails());		
		return result;

	}
	
	    
	public Map<String,Values<LovMetadata>> findBy(String lovType, int pageNumber) {
		if(!ValidationUtil.isHavingValue(lovType) || lovType.isEmpty())
			throw new ConfigurationException("lovType should not be null or empty");
		
		if(pageNumber<=0)
			throw new ConfigurationException("pageNumber must not be less than or equal to zero");	

		Pageable pageable = (Pageable) PageRequest.of(pageNumber-1, pageSize, Sort.by("lovName").ascending());
		
		Specification<LOVTypeEntity> spec = Specification.where(null);
		spec = spec.and(LovMetadataSpecification.hasLovType(lovType));
	    
	    var lovTypePage = lovTypeRepository.findAll(spec,pageable);
	    
		var values = lovTypePage.getContent().stream().map(item -> modelMapper.map(item, LovMetadata.class)).collect(Collectors.collectingAndThen(Collectors.toList(), items -> new Values<LovMetadata>(items)));
		
		var map = new HashMap<String,Values<LovMetadata>>();
		map.put(lovType, values);
		return map;
	
		}
	
	public Map<String,Values<LovMetadata>> findAll(String lovType, String lovName, String lovValue) {
		
		Specification<LOVTypeEntity> spec = Specification.where(null);
		
		if (lovType != null && !lovType.isEmpty()) {
            spec = spec.and(LovMetadataSpecification.hasLovType(lovType));
        }

        if (lovName != null && !lovName.isEmpty()) {
            spec = spec.and(LovMetadataSpecification.hasLovName(lovName));
        }

        if (lovValue != null && !lovValue.isEmpty()) {
            spec = spec.and(LovMetadataSpecification.hasLovValue(lovValue));
        }
        
        Pageable pageable = (Pageable) PageRequest.of(0, pageSize, Sort.by("lovName").ascending());
        
        var lovTypePage = lovTypeRepository.findAll(spec,pageable);      
    
		return lovTypePage.stream().map(item -> modelMapper.map(item, LovMetadata.class)).
				collect(Collectors.collectingAndThen(Collectors.groupingBy(LovMetadata::getLovType), map ->  map.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> new Values<LovMetadata>(entry.getValue())))));
		
	}
	
	@Transactional(readOnly = true)
	public SXSSFWorkbook exportData(String type) {
		SXSSFWorkbook workbook = new SXSSFWorkbook(pageSize);
	    var sheet = workbook.createSheet(type);
	        
	    Row header = sheet.createRow(0);
	    header.createCell(0).setCellValue("LOV_TYPE");
	    header.createCell(1).setCellValue("LOV_NAME");
	    header.createCell(2).setCellValue("LOV_VALUE");
		int[] rowNum = {1};
		try(var lovs = lovTypeRepository.findByLovType(type)) {
			lovs.forEach(lov -> {
				Row row = sheet.createRow(rowNum[0]++);
				row.createCell(0).setCellValue(lov.getLovType());
				row.createCell(1).setCellValue(lov.getLovName());
				row.createCell(2).setCellValue(lov.getLovValue());
				entityManager.detach(lov);
			});
		}	
        return workbook;
    }
	
	private LOVTypeEntity getTrimmedLovData(LOVTypeEntity lovTypeEntity) {
		if(ValidationUtil.isHavingValue(lovTypeEntity.getLovType())){
		lovTypeEntity.setLovType(lovTypeEntity.getLovType().strip());
		}
		if(ValidationUtil.isHavingValue(lovTypeEntity.getLovName())){
		lovTypeEntity.setLovName(lovTypeEntity.getLovName().strip());
		}
		if(ValidationUtil.isHavingValue(lovTypeEntity.getLovValue())){
		lovTypeEntity.setLovValue(lovTypeEntity.getLovValue().strip());
		}
		return lovTypeEntity;
	}
	

}
