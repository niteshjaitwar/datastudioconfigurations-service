package com.adp.esi.digitech.ds.config.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.adp.esi.digitech.ds.config.entity.ComplexLOVTypeEntity;
import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.model.ComplexLovData;
import com.adp.esi.digitech.ds.config.model.Pagination;
import com.adp.esi.digitech.ds.config.repo.ComplexLovTypeRepository;
import com.adp.esi.digitech.ds.config.util.ValidationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ComplexLOVMetadataService extends AbstractConfigService<ComplexLovData> {

	@Autowired
	private ComplexLovTypeRepository complexLovTypeRepository;

	@Autowired
	DataFormatter dataFormatter;

	@Value("${datastudio.excel.type}")
	private String excelFileType;
	
	@Value("${pagination.lov.fetch.size}")
	int pageSize;
	
	@Autowired
	ObjectMapper objectMapper;
	
	public static final String COMPLEX = "complex";
	
	public static final String LOV_TYPE = "LOV_TYPE";
	public static final String VALUE = "VALUE";

	public Map<String, Pagination> getAllLovTypeBasicDetails() {

		return null;
	}
	
	public Map<String, Pagination> getAllComplexLovTypeBasicDetails() {

		Map<String, Pagination> result = new LinkedHashMap<>();

		List<Object[]> complexList = complexLovTypeRepository.findAllComplexLovTypeDetails();

		for (Object[] obj : complexList) {
			String lovType = String.valueOf(obj[0]);
			var count = ((BigDecimal) obj[1]).intValue();
			String lovSchema = String.valueOf(obj[2]);
			var pages = (count + pageSize - 1) / pageSize;
			var pagenation = Pagination.builder().count(count).pages(pages).type(ComplexLOVMetadataService.COMPLEX).schema(lovSchema).build();
			result.put(lovType, pagenation);
		}
		return result;
	}

	public void saveEntity(MultipartFile file) throws IOException {

		if (file == null || file.isEmpty()) {
			throw new ConfigurationException("Invalid Data, File can't be null or empty");
		}
		if (!excelFileType.equalsIgnoreCase(file.getContentType()))
			throw new ConfigurationException("Invalid File Format, Only Excel File is allowed");

		try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				processSheet(workbook.getSheetAt(i));
			}
		}
	}
	
	private void processSheet(Sheet sheet) throws IOException {
		BiFunction<Row ,Integer, String> rowFunction = (row, index) -> dataFormatter.formatCellValue(row.getCell(index)).trim();
		
		
		if (sheet.getPhysicalNumberOfRows() <= 1) {
			log.debug("Sheet '{}' has no data rows. Skipping.", sheet.getSheetName());
			return;
		}
		
		Row headerRow = sheet.getRow(0);				
		if (headerRow == null) {
			throw new ConfigurationException("Sheet '" + sheet.getSheetName() + "' is missing a header row");
		}
		
		int colCount = headerRow.getPhysicalNumberOfCells();		
		
		
		if (colCount < 3) {
			throw new ConfigurationException("Each sheet must have at least one LovType column, DependsOn coumn and value column");
		}
		
		if(!ComplexLOVMetadataService.LOV_TYPE.equalsIgnoreCase(rowFunction.apply(headerRow, 0)))
			throw new ConfigurationException("Sheet '" + sheet.getSheetName() + "' is missing a header row LOV_TYPE as first column");
		
		if(!ComplexLOVMetadataService.VALUE.equalsIgnoreCase(rowFunction.apply(headerRow, colCount-1)))
			throw new ConfigurationException("Sheet '" + sheet.getSheetName() + "' is missing a header row VALUE as last column");
		
		
		Map<String, String> schema = new HashMap<>();
		for (int c = 1; c < colCount - 1; c++) {
			schema.put(String.valueOf(c-1), rowFunction.apply(headerRow, c));
		}
		

		List<Map<String, String>> records = new ArrayList<>();
		var lovType = "";
		for (int r = 1; r < sheet.getPhysicalNumberOfRows(); r++) {
			Row row = sheet.getRow(r);
			if (row == null)
				continue;
			if(!ValidationUtil.isHavingValue(lovType))
				lovType = rowFunction.apply(row, 0);
			
			Map<String, String> rowMap = new LinkedHashMap<>();
			for (int c = 1; c < colCount - 1; c++) {
				rowMap.put(String.valueOf(c-1), rowFunction.apply(row, c));
			}
			rowMap.put(ComplexLOVMetadataService.VALUE, rowFunction.apply(row, colCount-1));
			records.add(rowMap);
		}

		var lovDataJson = objectMapper.writeValueAsString(records);
		var schemaJson = objectMapper.writeValueAsString(schema);
		ComplexLOVTypeEntity entity = new ComplexLOVTypeEntity();
		entity.setLovType(lovType);
		entity.setLovDataJson(lovDataJson);
		entity.setLovRecordsCount(records.size());
		entity.setLovSchema(schemaJson);
		complexLovTypeRepository.save(entity);

	}
	
	public ComplexLovData updateEntity(String lovType, String json) throws IOException {

		if (json == null || json.isEmpty()) {
			throw new ConfigurationException("Invalid data : JSON cannot be null or empty");
		}

		ComplexLOVTypeEntity existingEntity = complexLovTypeRepository.findByLovType(lovType)
				.orElseThrow(() -> new ConfigurationException("Entity with lovType '" + lovType + "'not found"));

		List<Map<String, String>> records = parseAndValidateJson(json);

		existingEntity.setLovDataJson(json);
		existingEntity.setLovRecordsCount(records.size());
		
		var entity = complexLovTypeRepository.save(existingEntity);

		return modelMapper.map(entity, ComplexLovData.class);
	}

	
	
	private List<Map<String, String>> parseAndValidateJson(String json) throws IOException {
		try {
			List<Map<String, String>> records = objectMapper.readValue(json, new TypeReference<>() {});

			if (records.isEmpty()) {
				throw new ConfigurationException("At least one record required");
			}

			records.forEach(record -> {
				if (!record.containsKey(ComplexLOVMetadataService.VALUE)) {
					throw new ConfigurationException("Missing 'Value' field in record: " + record);
				}
			});
			return records;
		} catch (JsonProcessingException e) {
			throw new ConfigurationException("Invalid JSON: " + e.getMessage());
		}
	}
	
	public ComplexLovData findByLovType(String lovType) {

		var entity =  complexLovTypeRepository.findByLovType(lovType)
				.orElseThrow(() -> new EntityNotFoundException("LovType not found: " + lovType));
		
		return modelMapper.map(entity, ComplexLovData.class);
		
		
		/*
		var json = complexLovData.getLovDataJson();

		try {
			return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Invalid Json for LovType: " + lovType, e);
		}
		*/
	}
	
	@Transactional(readOnly = true)
	public SXSSFWorkbook exportComplexData(String lovType) throws IOException {
		SXSSFWorkbook workbook = new SXSSFWorkbook(pageSize);
		Sheet sheet = workbook.createSheet(lovType);

		ComplexLOVTypeEntity entity = complexLovTypeRepository.findByLovType(lovType)
				.orElseThrow(() -> new ConfigurationException("Type not found"));

		Map<String, String> headerMap = objectMapper.readValue(entity.getLovSchema(), new TypeReference<Map<String, String>>() {});

		List<String> orderedheaders = IntStream.range(0, headerMap.size()).mapToObj(String::valueOf).map(headerMap::get)
				.collect(Collectors.toList());

		List<Map<String, String>> records = objectMapper.readValue(entity.getLovDataJson(),	new TypeReference<List<Map<String, String>>>() {});

		Row headerRow = sheet.createRow(0);
		AtomicInteger counter = new AtomicInteger();
		Stream.concat(
						Stream.of(ComplexLOVMetadataService.LOV_TYPE), 
						Stream.concat(orderedheaders.stream(), 
						Stream.of(ComplexLOVMetadataService.VALUE))
					 ).forEachOrdered(value -> headerRow.createCell(counter.getAndIncrement()).setCellValue(value));

		AtomicInteger rowNum = new AtomicInteger(1);

		records.forEach(record -> {
			Row row = sheet.createRow(rowNum.getAndIncrement());
			counter.set(0);
			Stream.concat(
							Stream.of(lovType), 
							Stream.concat(IntStream.range(0, orderedheaders.size()).mapToObj(i -> record.getOrDefault(String.valueOf(i), "")),
							Stream.of(record.getOrDefault(ComplexLOVMetadataService.VALUE, "")))
						 ).forEachOrdered(value -> row.createCell(counter.getAndIncrement()).setCellValue(value));
		});

		//IntStream.range(0, allHeaders.size()).forEach(sheet::autoSizeColumn);
		return workbook;
	}

	

	@Override
	public List<ComplexLovData> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ComplexLovData findById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ComplexLovData saveEntity(ComplexLovData data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ComplexLovData> saveEntities(List<ComplexLovData> data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ComplexLovData updateSingle(ComplexLovData data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ComplexLovData> updateBulk(List<ComplexLovData> data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ComplexLovData> patch(List<Map<String, String>> configurations) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteSingle(Long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteBulk(List<Long> ids) {
		// TODO Auto-generated method stub

	}

}
