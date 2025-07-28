package com.adp.esi.digitech.ds.config.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.model.ApiResponse;
import com.adp.esi.digitech.ds.config.model.ComplexLovData;
import com.adp.esi.digitech.ds.config.model.ErrorResponse;
import com.adp.esi.digitech.ds.config.service.ComplexLOVMetadataService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Validated
@RestController
@Slf4j
@RequestMapping("/ahub/ds/config/complex-lov-metadata")
@CrossOrigin(origins = "${app.allowed-origins}")
@Tag(name = "Complex Lov Metadata", description = "")
public class ComplexLOVMetadataController {
	
	@Autowired
	ComplexLOVMetadataService complexLOVMetadataService;
	
	@PatchMapping
	public ResponseEntity<ApiResponse<ComplexLovData>> partialupdateComplexLOVMetaData(@RequestParam(name="type", required = true) String type, @RequestParam(name="json", required = true) String json) {
		ApiResponse<ComplexLovData> response = null;
		try {
			var data = complexLOVMetadataService.updateEntity(type, json);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ComplexLOVMetadataController - partialupdateComplexLOVMetaData()  Completed Partial Updating Data, lovType = {}",type);
		} catch (ConfigurationException e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ComplexLOVMetadataController - partialupdateComplexLOVMetaData()  Failed to Partial Update Data for lovType = {}, Exception Message : {} ", type, e.getMessage());
			return ResponseEntity.badRequest().body(response);
		} catch (IOException e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ComplexLOVMetadataController - partialupdateComplexLOVMetaData()  Failed to Partial Update Data for lovType = {}, Exception Message : {} ", type, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
		return ResponseEntity.ok().body(response);
	}
	
	
	
	@PostMapping("/upload")
	public ResponseEntity<ApiResponse<String>> addAllComplexLOVMetaDataByFile(
			@RequestParam("lovfile") MultipartFile file) {
		log.info("ComplexLOVMetadataController - addAllComplexLOVMetaDataByFile () started Saving Data, file name ={}",
				file.getOriginalFilename());
		
		ApiResponse<String> response = null;
		
		try {
			complexLOVMetadataService.saveEntity(file);
			response = ApiResponse
					.success(ApiResponse.Status.SUCCESS, "LOV Metadata uploaded and saved successfully");
			log.info("ComplexLOVMetadataController - addAllComplexLOVMetaDataByFile() completed successfully for file: {}",
					file.getOriginalFilename());
			return ResponseEntity.ok(response);
		}catch(Exception e) {
			log.error("ComplexLOVMetadataController - addAllComplexLOVMetaDataByFile() failed for file: {}",
					file.getOriginalFilename());
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			return ResponseEntity.internalServerError().body(response);
		}
	}
	
	@PostMapping("/by/type")
	public ResponseEntity<ApiResponse<ComplexLovData>> getAllComplexLOVMetadataByType(@RequestParam(name="type", required = true) String type) {
		log.info("ComplexLOVMetadataController - getAllComplexLOVMetadataByType()  Started Retrieving Data of type = {}", type);
		ApiResponse<ComplexLovData> response = null;
		try {
			var data = complexLOVMetadataService.findByLovType(type);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			return ResponseEntity.ok(response);
		} catch (EntityNotFoundException e) {
			log.error("ComplexLOVMetadataController - getAllComplexLOVMetadataByType() lovType not found: {}", type);
			ErrorResponse error = new ErrorResponse("404", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			return ResponseEntity.status(HttpStatusCode.valueOf(404)).body(response);
		} catch(Exception e) {
			log.error("ComplexLOVMetadataController - getAllComplexLOVMetadataByType() failed to get lovType: {}", type);
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			return ResponseEntity.internalServerError().body(response);
		}
		
	}

	@PostMapping("/by/type/export")
	public ResponseEntity<Resource> exportData(@RequestParam(name="type", required = true) String type){
		log.info("ComplexLOVMetadataController - exportData() exporting type= {}", type);
		
		var headers= new HttpHeaders();
		SXSSFWorkbook workbook= null;
		ByteArrayOutputStream output= null;
		
		try {
			workbook = complexLOVMetadataService.exportComplexData(type);
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", type.strip() + ".xlsx");
			
			output= new ByteArrayOutputStream();
			
			workbook.write(output);
			
			return ResponseEntity.ok().headers(headers).body(new ByteArrayResource(output.toByteArray()));
		}catch(Exception e) {
			log.error("ComplexLOVMetadataController - exportData() Failed exporting data of type = {}, Exception Message : {} ", type, e.getMessage());
			return ResponseEntity.internalServerError().build();
		}finally {
			try {
				if(workbook != null) {
					workbook.close();
				}
				if(output != null) {
					output.close();
				}
			}catch(Exception e) {
				log.error("ComplexLOVMetadataController - exportData() Failed to close resources, type = {}, Exception Message : {} ", type, e.getMessage());
			}
		}
	}
}
