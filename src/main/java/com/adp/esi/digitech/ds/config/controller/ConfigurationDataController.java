package com.adp.esi.digitech.ds.config.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adp.esi.digitech.ds.config.model.ApiResponse;
import com.adp.esi.digitech.ds.config.model.ConfigurationData;
import com.adp.esi.digitech.ds.config.model.ErrorResponse;
import com.adp.esi.digitech.ds.config.service.ConfigurationDataService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;

@Validated
@RestController
@RequestMapping("/ahub/ds/config")
@Slf4j
@CrossOrigin(origins = "${app.allowed-origins}")
@Tag(name = "Configuration Data", description = "")
public class ConfigurationDataController {
	
	@Autowired
	ConfigurationDataService configurationDataService;
	
	@Operation(hidden = true)
	@GetMapping
	public ResponseEntity<ApiResponse<List<ConfigurationData>>> getAllConfigurationData() {
		log.info("ConfigurationDataController - getAllConfigurationData()  Started Retrieving Data");
		ApiResponse<List<ConfigurationData>> response = null;
		try {
			var data = configurationDataService.findAll();
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ConfigurationDataController - getAllConfigurationData()  Completed Retrieving Data");
			
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ConfigurationDataController - getAllConfigurationData()  Failed Retrieving Data. Exception Message : {} ", e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@GetMapping("/basic-info")
	public ResponseEntity<ApiResponse<Map<String, Map<String, List<String>>>>> getBUPlatformAndDataCategoryBasicInfo() {
		log.info("ConfigurationDataController - getBUPlatformAndDataCategoryBasicInfo()  Started  Retrieving Data");
		ApiResponse<Map<String, Map<String, List<String>>>> response = null;		
		try {			
			var data = configurationDataService.getBUPlatformAndDataCategoryBasicInfo();
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ConfigurationDataController - getBUPlatformAndDataCategoryBasicInfo()  Completed Retrieving Data");
			
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ConfigurationDataController - getBUPlatformAndDataCategoryBasicInfo()  Failed Retrieving Data. Exception Message : {} ", e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}
		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/by")
	public ResponseEntity<ApiResponse<ConfigurationData>> getConfigurationDataBy(@RequestParam(name = "bu", required = true) String bu
			  																		,@RequestParam(name = "platform", required = true) String platform
			  																		,@RequestParam(name = "dataCategory", required = true) String dataCategory) {
		log.info("ConfigurationDataController - getConfigurationDataBy()  Started Retrieving Data By. bu = {}, platform = {}, dataCategory = {}", bu, platform, dataCategory);
		ApiResponse<ConfigurationData> response = null;
		try {
			var data = configurationDataService.findBy(bu, platform, dataCategory);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ConfigurationDataController - getConfigurationDataBy()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ConfigurationDataController - getConfigurationDataBy()  Failed Retrieving Data. bu = {}, platform = {}, dataCategory = {}, Exception Message : {} ", bu, platform, dataCategory, e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/by/bulk")
	public ResponseEntity<ApiResponse<List<ConfigurationData>>> getConfigurationDataByBulk(@Valid @RequestBody List<ConfigurationData> configurations) {
		log.info("ConfigurationDataController - getConfigurationDataByBulk()  Started Retrieving Data By. configurations = {}", configurations);
		ApiResponse<List<ConfigurationData>> response = null;
		try {
			var data = configurationDataService.findBy(configurations);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ConfigurationDataController - getConfigurationDataByBulk()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ConfigurationDataController - getConfigurationDataByBulk()  Failed Retrieving Data. configurations = {}, Exception Message : {} ", configurations, e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ConfigurationData>> getConfigurationDataById(@PathVariable("id") long id) {
		log.info("ConfigurationDataController - getConfigurationDataById()  Started Retrieving Data by id = {}",id);
		ApiResponse<ConfigurationData> response = null;		
		try {			
			var data = configurationDataService.findById(id);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ConfigurationDataController - getConfigurationDataById()  Completed Retrieving Data for id = {}",id);
			
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ConfigurationDataController - getConfigurationDataById()  Failed Retrieving Data for id = {}, Exception Message : {} ", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}
		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping
	public ResponseEntity<ApiResponse<ConfigurationData>> addConfigurationData(@Valid	@RequestBody ConfigurationData configurationData) {
		log.info("ConfigurationDataController - addConfigurationData()  Started Saving Data, configurationData = {}", configurationData);
		ApiResponse<ConfigurationData> response = null;

		try {
			var data = configurationDataService.saveEntity(configurationData);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ConfigurationDataController - addConfigurationData()  Completed Saving Data, configurationData = {}",data);
			
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ConfigurationDataController - addConfigurationData()  Failed to save Data for configurationData = {}, Exception Message : {} ", configurationData, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/bulk")
	public ResponseEntity<ApiResponse<List<ConfigurationData>>> addAllConfigurationData(
			@RequestBody @NotEmpty(message = "Input Configuration Data cannot be empty.")List<@Valid ConfigurationData> configurationDataEntities) {
		log.info("ConfigurationDataController - addAllConfigurationData()  Started Saving Data, configurationDatas = {}", configurationDataEntities);
		ApiResponse<List<ConfigurationData>> response = null;

		try {
			var data = configurationDataService.saveEntities(configurationDataEntities);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ConfigurationDataController - addAllConfigurationData()  Completed Saving Data, validationRules = {}",data);
			
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ConfigurationDataController - addAllConfigurationData()  Failed to save Data for validationRules = {}, Exception Message : {} ", configurationDataEntities, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
		return ResponseEntity.ok().body(response);
		
	}

	@PutMapping
	public ResponseEntity<ApiResponse<ConfigurationData>> updateConfigurationData(
			@Valid @RequestBody ConfigurationData configurationData) {
		log.info("ConfigurationDataController - updateConfigurationData()  Started Updating Data, configurationData = {}", configurationData);
		ApiResponse<ConfigurationData> response = null;

		try {		
			var data = configurationDataService.updateSingle(configurationData);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ConfigurationDataController - updateConfigurationData()  Completed Updating Data, configurationData = {}",data);
			
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ConfigurationDataController - updateConfigurationData()  Failed to Update Data for configurationData = {}, Exception Message : {} ", configurationData, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
		return ResponseEntity.ok().body(response);

	}
	/*
	 * 	please refer to JpaRepository class
	 * 	use findAllById --> List<T> findAllById(Iterable<ID> ids) method to get all
	 * 	data at once
	 * 	use saveAll --> <S extends T> List<S> saveAll(Iterable<S> entities) method to
	 * 	update all records at once, saveAll will also do upsert functionality
	 * 	implement these logics in service class ConfigurationDataService
	*/
	@PutMapping("/bulk")
	public ResponseEntity<ApiResponse<List<ConfigurationData>>> updateAllConfigurationData(
			@RequestBody @NotEmpty(message = "Input Configuration Data cannot be empty.") List<@Valid ConfigurationData> configurationDatas) {
		log.info("ConfigurationDataController - updateAllConfigurationData()  Started Updating Data, configurationDatas = {}", configurationDatas);
		ApiResponse<List<ConfigurationData>> response = null;
		try {
			var data = configurationDataService.updateBulk(configurationDatas);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ConfigurationDataController - updateAllConfigurationData()  Completed Updating Data, configurationDatas = {}",data);
			
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ConfigurationDataController - updateAllConfigurationData()  Failed to Update Data for configurationDatas = {}, Exception Message : {} ", configurationDatas, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PatchMapping
	public ResponseEntity<ApiResponse<List<ConfigurationData>>> partialupdateAllConfigurationData(
			@RequestBody List<Map<String, String>> configurationDatas) {
		log.info("ConfigurationDataController - partialupdateAllConfigurationData()  Started Partial Updating Data, configurationDatas = {}", configurationDatas);
		ApiResponse<List<ConfigurationData>> response = null;
		try {
			var data = configurationDataService.patch(configurationDatas);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ConfigurationDataController - partialupdateAllConfigurationData()  Completed Partial Updating Data, configurationDatas = {}",data);
			
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ConfigurationDataController - partialupdateAllConfigurationData()  Failed to Partial Update Data for configurationDatas = {}, Exception Message : {} ", configurationDatas, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<String>> deleteConfigurationDataById(@PathVariable("id") long id) {
		log.info("ConfigurationDataController - deleteConfigurationDataById()  Started deleting configuration data by id = {}",id);
		ApiResponse<String> response = null;

		try {
			configurationDataService.deleteSingle(id);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Deleted Successfully");
			log.info("ConfigurationDataController - deleteConfigurationDataById()  Completed deleting configuration data by id = {}",id);
			
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ConfigurationDataController - deleteConfigurationDataById()  Failed to delete configuration data for id = {}, Exception Message : {} ", id, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@DeleteMapping("/bulk")
	public ResponseEntity<ApiResponse<String>> deleteConfigurationDataById(@RequestBody List<Long> ids) {
		log.info("ConfigurationDataController - deleteConfigurationDataById()  Started deleting configuration data by id's = {}",ids);
		ApiResponse<String> response = null;

		try {
			configurationDataService.deleteBulk(ids);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Deleted Successfully");
			log.info("ConfigurationDataController - deleteConfigurationDataById()  Completed deleting configuration data by id's = {}",ids);
			
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ConfigurationDataController - deleteConfigurationDataById()  Failed to delete configuration data for id's = {}, Exception Message : {} ", ids, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
}
