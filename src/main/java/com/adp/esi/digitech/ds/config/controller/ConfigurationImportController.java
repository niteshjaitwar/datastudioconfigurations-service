package com.adp.esi.digitech.ds.config.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.adp.esi.digitech.ds.config.model.ApiResponse;
import com.adp.esi.digitech.ds.config.model.DVTSVersioning;
import com.adp.esi.digitech.ds.config.model.ErrorResponse;
import com.adp.esi.digitech.ds.config.service.ConfigurationImportService;
import com.adp.esi.digitech.ds.config.util.FileUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Validated
@RestController
@RequestMapping("/ahub/ds/config/import")
@CrossOrigin(origins = "${app.allowed-origins}")
@Tag(name = "Configuration Import", description = "")
public class ConfigurationImportController {
	
	@Autowired
	private ConfigurationImportService configExportImportService;
	
	@Autowired
	FileUtils fileUtils;
	
	@PostMapping
	public ResponseEntity<ApiResponse<String>> importData(@Valid DVTSVersioning versioning ,@RequestParam(name = "file", required = true) MultipartFile file) throws JsonMappingException, JsonProcessingException {
		log.info("ConfigExportImportController - importData() started importing data of bu = {}, platform = {}, dataCategory = {}", versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());
		ApiResponse<String> response = null;
		if(!MediaType.TEXT_PLAIN_VALUE.equals(fileUtils.getContentType(file.getOriginalFilename()))) {
			ErrorResponse error = new ErrorResponse("400", "Uploaded file is not a txt file");
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ConfigurationImportController - importData() Failed importing data of bu = {}, platform = {}, dataCategory = {}, Exception Message : {} ",
					versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory(), "Uploaded file is not a txt file");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		try {
			configExportImportService.importData(versioning, file);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Configurations are imported successfully");
			log.info("ConfigExportImportController - importData() completed importing data of bu = {}, platform = {}, dataCategory = {}", versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory());
		} catch (IOException e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error(
					"ConfigurationImportController - importData() Failed importing data of bu = {}, platform = {}, dataCategory = {}, Exception Message : {} ",
					versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory(), e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
		return ResponseEntity.ok().body(response);
	}

}
