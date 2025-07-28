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
import com.adp.esi.digitech.ds.config.model.ErrorResponse;
import com.adp.esi.digitech.ds.config.model.FilePollingConfig;
import com.adp.esi.digitech.ds.config.service.FilePollingConfigService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;

@Validated
@Slf4j
@RestController
@RequestMapping("/ahub/ds/config/file/poll")
@CrossOrigin(origins = "${app.allowed-origins}")
@Tag(name = "File Polling", description = "")
public class FilePollingConfigController {
	
	@Autowired
	FilePollingConfigService filePollingConfigService;
	
	@GetMapping
	@Operation(hidden = true)
	public ResponseEntity<ApiResponse<List<FilePollingConfig>>> getFilePollingConfigs() {
		log.info("FilePollingConfigController - getFilePollingConfigs() Started Retrieving file polling config Data");
		ApiResponse<List<FilePollingConfig>> response = null;
		try {
			var data = filePollingConfigService.findAll();
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("FilePollingConfigController - getFilePollingConfigs() Completed Retrieving file polling config Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("FilePollingConfigController - getFilePollingConfigs() Failed Retrieving file polling config Data. Exception Message : {} ", e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<FilePollingConfig>> getFilePollingConfigById(@PathVariable("id") long id) {
		log.info("FilePollingConfigController - getFilePollingConfigById() Started Retrieving file polling config Data by id = {}",id);
		ApiResponse<FilePollingConfig> response = null;	
		try {			
			var data = filePollingConfigService.findById(id);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("FilePollingConfigController - getFilePollingConfigById() Completed Retrieving file polling config Data for id = {}",id);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("FilePollingConfigController - getFilePollingConfigById() Failed Retrieving file polling config Data for id = {}, Exception Message : {} ", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}
		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/by")
	public ResponseEntity<ApiResponse<FilePollingConfig>> getFilePollingConfigByBu(@RequestParam(name = "bu", required = true) String bu) {
		log.info("FilePollingConfigController - getFilePollingConfigByBu() Started Retrieving file polling config data by bu = {}", bu);
		ApiResponse<FilePollingConfig> response = null;
		try {
			var data = filePollingConfigService.findByBu(bu);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("FilePollingConfigController - getFilePollingConfigByBu() Completed Retrieving file polling config data by bu = {}", bu);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("FilePollingConfigController - getFilePollingConfigByBu() Failed Retrieving file polling config data bu = {}, Exception Message : {} ", bu, e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping
	public ResponseEntity<ApiResponse<FilePollingConfig>> addFilePollingConfig(@Valid @RequestBody FilePollingConfig filePollingConfig) {
		log.info("FilePollingConfigController - addFilePollingConfig() Started adding file polling config Data, filePollingConfig = {}", filePollingConfig);
		ApiResponse<FilePollingConfig> response = null;

		try {
			var data = filePollingConfigService.saveEntity(filePollingConfig);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("FilePollingConfigController - addFilePollingConfig() Completed adding file polling config Data, filePollingConfig = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("FilePollingConfigController - addFilePollingConfig() Failed to adding file polling config Data for filePollingConfig = {}, Exception Message : {} ", filePollingConfig, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/bulk")
	public ResponseEntity<ApiResponse<List<FilePollingConfig>>> addFilePollingConfigs(
			@RequestBody @NotEmpty(message = "File Polling Config cannot be empty.")List<@Valid FilePollingConfig> filePollingConfigs) {
		log.info("FilePollingConfigController - addFilePollingConfigs() Started adding file polling config Data, filePollingConfigs = {}", filePollingConfigs);
		ApiResponse<List<FilePollingConfig>> response = null;

		try {
			var data = filePollingConfigService.saveEntities(filePollingConfigs);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("FilePollingConfigController - addFilePollingConfigs() Completed adding file polling config Data, filePollingConfigs = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("FilePollingConfigController - addFilePollingConfigs() Failed to save Data for filePollingConfigs = {}, Exception Message : {} ", filePollingConfigs, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PutMapping
	public ResponseEntity<ApiResponse<FilePollingConfig>> updateFilePollingConfig(
			@Valid @RequestBody FilePollingConfig FilePollingConfig) {
		log.info("FilePollingConfigController - updateFilePollingConfig()  Started Updating Data, FilePollingConfig = {}", FilePollingConfig);
		ApiResponse<FilePollingConfig> response = null;

		try {		
			var data = filePollingConfigService.updateSingle(FilePollingConfig);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("FilePollingConfigController - updateFilePollingConfig()  Completed Updating Data, FilePollingConfig = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("FilePollingConfigController - updateFilePollingConfig()  Failed to Update Data for FilePollingConfig = {}, Exception Message : {} ", FilePollingConfig, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
		return ResponseEntity.ok().body(response);

	}
	
	@PutMapping("/bulk")
	public ResponseEntity<ApiResponse<List<FilePollingConfig>>> updateFilePollingConfigs(
			@RequestBody @NotEmpty(message = "Input file polling config cannot be empty.") List<@Valid FilePollingConfig> filePollingConfigs) {
		log.info("FilePollingConfigController - updateFilePollingConfigs() Started Updating Data, filePollingConfigs = {}", filePollingConfigs);
		ApiResponse<List<FilePollingConfig>> response = null;
		try {
			var data = filePollingConfigService.updateBulk(filePollingConfigs);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("FilePollingConfigController - updateFilePollingConfigs() Completed Updating Data, filePollingConfigs = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("FilePollingConfigController - updateFilePollingConfigs() Failed to Update Data for filePollingConfigs = {}, Exception Message : {} ", filePollingConfigs, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PatchMapping
	public ResponseEntity<ApiResponse<List<FilePollingConfig>>> partialupdateFilePollingConfig(
			@RequestBody List<Map<String, String>> FilePollingConfigs) {
		log.info("FilePollingConfigController - partialupdateFilePollingConfig()  Started Partial Updating Data, FilePollingConfigs = {}", FilePollingConfigs);
		ApiResponse<List<FilePollingConfig>> response = null;
		try {
			var data = filePollingConfigService.patch(FilePollingConfigs);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("FilePollingConfigController - partialupdateFilePollingConfig()  Completed Partial Updating Data, FilePollingConfigs = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("FilePollingConfigController - partialupdateFilePollingConfig()  Failed to Partial Update Data for FilePollingConfigs = {}, Exception Message : {} ", FilePollingConfigs, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<String>> deleteFilePollingConfigById(@PathVariable("id") long id) {
		log.info("FilePollingConfigController - deleteFilePollingConfigById()  Started deleting File Polling Config by id = {}",id);
		ApiResponse<String> response = null;

		try {
			filePollingConfigService.deleteSingle(id);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Deleted Successfully");
			log.info("FilePollingConfigController - deleteFilePollingConfigById()  Completed deleting File Polling Config by id = {}",id);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("FilePollingConfigController - deleteFilePollingConfigById()  Failed to delete File Polling Config for id = {}, Exception Message : {} ", id, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@DeleteMapping("/bulk")
	public ResponseEntity<ApiResponse<String>> deleteFilePollingConfigById(@RequestBody List<Long> ids) {
		log.info("FilePollingConfigController - deleteFilePollingConfigById()  Started deleting File Polling Config by id's = {}",ids);
		ApiResponse<String> response = null;

		try {
			filePollingConfigService.deleteBulk(ids);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Deleted Successfully");
			log.info("FilePollingConfigController - deleteFilePollingConfigById()  Completed deleting File Polling Config by id's = {}",ids);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("FilePollingConfigController - deleteFilePollingConfigById()  Failed to delete File Polling Config for id's = {}, Exception Message : {} ", ids, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
}
