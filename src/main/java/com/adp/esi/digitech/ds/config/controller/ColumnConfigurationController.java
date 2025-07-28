package com.adp.esi.digitech.ds.config.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.model.ApiResponse;
import com.adp.esi.digitech.ds.config.model.ColumnConfigData;
import com.adp.esi.digitech.ds.config.model.ColumnConfiguration;
import com.adp.esi.digitech.ds.config.model.ColumnConfigurationResponse;
import com.adp.esi.digitech.ds.config.model.ConfigurationData;
import com.adp.esi.digitech.ds.config.model.ErrorResponse;
import com.adp.esi.digitech.ds.config.model.ValidationInfo;
import com.adp.esi.digitech.ds.config.service.ColumnConfigurationService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;

@Validated
@Slf4j
@RestController
@RequestMapping("/ahub/ds/config/column-configuration")
@CrossOrigin(origins = "${app.allowed-origins}")
@Tag(name = "Column Configurations", description = "")
public class ColumnConfigurationController {
	
	@Autowired
	ColumnConfigurationService columnConfigurationService; 
	
	@PostMapping("/by")
	public ResponseEntity<ApiResponse<ColumnConfigurationResponse>> getAllColumnConfigurationsBy(@RequestParam(name = "bu", required = true) String bu
																									  ,@RequestParam(name = "platform", required = true) String platform
																									  ,@RequestParam(name = "dataCategory", required = true) String dataCategory) {
		log.info("ColumnConfigurationController - getAllColumnConfigurationsBy()  Started Retrieving Data By. bu = {}, platform = {}, dataCategory = {}", bu, platform, dataCategory);
		ApiResponse<ColumnConfigurationResponse> response = null;
		try {
			var data = columnConfigurationService.findBy(bu, platform, dataCategory);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ColumnConfigurationController - getAllColumnConfigurationsBy()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnConfigurationController - getAllColumnConfigurationsBy()  Failed Retrieving Data. bu = {}, platform = {}, dataCategory = {}, Exception Message : {} ", bu, platform, dataCategory, e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/bulk")
	public ResponseEntity<ApiResponse<ColumnConfigurationResponse>> addAllColumnConfigurations(
			@RequestBody @NotEmpty(message = "Input Column Configurations cannot be empty.")List<@Valid ColumnConfiguration> columnConfigurations) {
		log.info("ColumnConfigurationController - addAllColumnConfigurations()  Started Saving Data, columnConfigurations = {}", columnConfigurations);
		ApiResponse<ColumnConfigurationResponse> response = null;
		
		try {			
			var data = columnConfigurationService.addbulk(columnConfigurations);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS,data);		
			log.info("ColumnConfigurationController - addAllColumnConfigurations()  Completed Saving Data, columnConfigurations = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnConfigurationController - addAllColumnConfigurations()  Failed to save Data for columnConfigurations = {}, Exception Message : {} ", columnConfigurations, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
			return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/upload")
	public ResponseEntity<ApiResponse<ColumnConfigurationResponse>> addAllColumnConfigurationsByFile(@RequestParam(name = "bu", required = true) String bu
																									,@RequestParam(name = "platform", required = true) String platform
																									,@RequestParam(name = "dataCategory", required = true) String dataCategory 
																									,@RequestParam(name = "columnConfigurationsFile", required = true) MultipartFile columnConfigurationsFile) {
		log.info("ColumnConfigurationController - addAllColumnConfigurationsByFile()  Started Saving Data, columnConfigurationsFile name = {}", columnConfigurationsFile.getOriginalFilename());
		ApiResponse<ColumnConfigurationResponse> response = null;
		try {
			var data = columnConfigurationService.saveEntities(bu, platform, dataCategory, columnConfigurationsFile);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ColumnConfigurationController - addAllColumnConfigurationsByFile()  Completed Saving Data, columnConfigurationsFile = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnConfigurationController - addAllColumnConfigurationsByFile()  Failed to save Data for columnConfigurationsFile name = {}, Exception Message : {} ", columnConfigurationsFile.getOriginalFilename(), e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/validate")
	public ResponseEntity<ApiResponse<String>> validateConfigurationData(@RequestParam(name = "bu", required = true) String bu
			  																		,@RequestParam(name = "platform", required = true) String platform
			  																		,@RequestParam(name = "dataCategory", required = true) String dataCategory) {
		log.info("ColumnConfigurationController - validateConfigurationData() Started validating Data By. bu = {}, platform = {}, dataCategory = {}", bu, platform, dataCategory);
		ApiResponse<String> response = null;
		try {
			columnConfigurationService.validateConfigurationData(bu, platform, dataCategory);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Validated Successfully");
			log.info("ColumnConfigurationController - validateConfigurationData() Completed validating Data");
		} catch (ConfigurationException e) {
			ErrorResponse error = new ErrorResponse("400", e.getMessage(), e.getErrors());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnConfigurationController - validateConfigurationData() Failed validating Data. bu = {}, platform = {}, dataCategory = {}, Exception Message : {} ", bu, platform, dataCategory, e.getMessage());
			return ResponseEntity.badRequest().body(response);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnConfigurationController - validateConfigurationData() Failed validating Data. bu = {}, platform = {}, dataCategory = {}, Exception Message : {} ", bu, platform, dataCategory, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
		return ResponseEntity.ok().body(response);
	}
	
	@DeleteMapping("/by")
	public ResponseEntity<ApiResponse<String>> softDelete(@Valid @RequestBody ConfigurationData configData) {
		log.info("ColumnConfigurationController - softDelete() Started deleting Data Category for bu = {}, platform = {}, dataCategory = {}", configData.getBu(), configData.getPlatform(),configData.getDataCategory());
		ApiResponse<String> response = null;

		try {
			columnConfigurationService.softDeleteDataCategory(configData);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Data Category Deleted Successfully");
			log.info("ColumnConfigurationController - softDelete() Completed deleting Data Category for bu = {}, platform = {}, dataCategory = {}", configData.getBu(), configData.getPlatform(),configData.getDataCategory());
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnConfigurationController - softDelete() Failed to delete Data Category for bu = {}, platform = {}, dataCategory = {}, Exception Message : {} ", configData.getBu(), configData.getPlatform(),configData.getDataCategory(), e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@DeleteMapping("/by/field")
	public ResponseEntity<ApiResponse<List<ValidationInfo>>> softDeleteField(@Valid @RequestBody ColumnConfigData columnData) {
		log.info("ColumnConfigurationController - softDeleteField() Started deleting field for bu = {}, platform = {}, dataCategory = {}, UUID = {}", columnData.getBu(), columnData.getPlatform(),columnData.getDataCategory(), columnData.getUuids());
		ApiResponse<List<ValidationInfo>> response = null;
		
		try {
			var data = columnConfigurationService.softDeletefield(columnData);
			if(!data.isEmpty())
				response = ApiResponse.error(ApiResponse.Status.FAILED, "Unable to delete fileds, as being used in multiple configurations", data);
			else
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Fields Deleted Successfully", null);
			log.info("ColumnConfigurationController - softDeleteField() Completed deleting field bu = {}, platform = {}, dataCategory = {}, UUID = {}", columnData.getBu(), columnData.getPlatform(),columnData.getDataCategory(), columnData.getUuids());
		} catch (ConfigurationException e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage(),e.getErrors());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnConfigurationController - softDeleteField() Failed to delete field for bu = {}, platform = {}, dataCategory = {}, UUID = {}, Exception Message : {} ", columnData.getBu(), columnData.getPlatform(),columnData.getDataCategory(), columnData.getUuids(), e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnConfigurationController - softDeleteField() Failed to delete field for bu = {}, platform = {}, dataCategory = {}, UUID = {}, Exception Message : {} ", columnData.getBu(), columnData.getPlatform(),columnData.getDataCategory(), columnData.getUuids(), e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
		
		return ResponseEntity.ok().body(response);
	}
	
	@PatchMapping("/dc")
	public ResponseEntity<ApiResponse<String>> updateDataCategory(
			@RequestBody List<Map<String, String>> configurationData) {
		log.info("ColumnRelationController - updateDataCategory() Started updating Data Category for configurationData = {}", configurationData);
		ApiResponse<String> response = null;
		try {
			columnConfigurationService.updateDataCategory(configurationData);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Data Category updated successfully");
			log.info("ColumnRelationController - updateDataCategory()  Completed updating Data Category for configurationData = {}", configurationData);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnRelationController - updateDataCategory()  Failed updating Data Category for configurationData = {}", configurationData);
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}	
}
