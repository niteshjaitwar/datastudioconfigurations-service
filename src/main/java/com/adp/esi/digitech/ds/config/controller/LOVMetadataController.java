package com.adp.esi.digitech.ds.config.controller;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import com.adp.esi.digitech.ds.config.model.ApiResponse;
import com.adp.esi.digitech.ds.config.model.ErrorResponse;
import com.adp.esi.digitech.ds.config.model.LovMetadata;
import com.adp.esi.digitech.ds.config.model.Pagination;
import com.adp.esi.digitech.ds.config.model.Values;
import com.adp.esi.digitech.ds.config.service.LOVMetadataService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;

@Validated
@RestController
@Slf4j
@RequestMapping("/ahub/ds/config/lov-metadata")
@CrossOrigin(origins = "${app.allowed-origins}")
@Tag(name = "Lov Metadata", description = "")
public class LOVMetadataController {
	
	@Autowired
	LOVMetadataService lovMetadataService;
	
	@Operation(hidden = true)
	@GetMapping
	public ResponseEntity<ApiResponse<List<LovMetadata>>> getAllLOVMetadata() {
		log.info("LOVMetadataController - getAllLOVMetadata()  Started Retrieving Data");
		ApiResponse<List<LovMetadata>> response = null;
		try {
			var data = lovMetadataService.findAll();
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("LOVMetadataController - getAllLOVMetadata()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("LOVMetadataController - getAllLOVMetadata()  Failed Retrieving Data. Exception Message : {} ", e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<LovMetadata>> getLOVMeatadatById(@PathVariable("id") long id) {
		log.info("LOVMetadataController - getLOVMeatadatById()  Started Retrieving Data by id = {}",id);
		ApiResponse<LovMetadata> response = null;
		try {
			var data = lovMetadataService.findById(id);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("LOVMetadataController - getLOVMeatadatById()  Completed Retrieving Data for id = {}",id);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("LOVMetadataController - getLOVMeatadatById()  Failed Retrieving Data for id = {}, Exception Message : {} ", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);

	}
	
	@PostMapping
	public ResponseEntity<ApiResponse<LovMetadata>> addLOVMetadata(@Valid @RequestBody LovMetadata lovMetadata) {
		
		log.info("LOVMetadataController - addLOVMetadata()  Started Saving Data, lovMetadata = {}", lovMetadata);
		ApiResponse<LovMetadata> response = null;

		try {
			var data = lovMetadataService.saveEntity(lovMetadata);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("LOVMetadataController - addLOVMetadata()  Completed Saving Data, lovMetadata = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("LOVMetadataController - addLOVMetadata()  Failed to save Data for lovMetadata = {}, Exception Message : {} ", lovMetadata, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/bulk")
	public ResponseEntity<ApiResponse<List<LovMetadata>>> addAllLOVMetadata(
			@RequestBody @NotEmpty(message = "Input Lov Metadata cannot be empty.") List<@Valid LovMetadata> lovMetadatas) {
		log.info("LOVMetadataController - addAllLOVMetadata()  Started Saving Data, lovMetadatas = {}", lovMetadatas);
		ApiResponse<List<LovMetadata>> response = null;

		try {
			var data = lovMetadataService.saveEntities(lovMetadatas);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("LOVMetadataController - addAllLOVMetadata()  Completed Saving Data, lovMetadatas = {}",data.size());
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("LOVMetadataController - addAllLOVMetadata()  Failed to save Data for lovMetadatas = {}, Exception Message : {} ", lovMetadatas, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PutMapping
	public ResponseEntity<ApiResponse<LovMetadata>> updateLOVMetadata(
			@Valid @RequestBody LovMetadata lovMetadata) {
		log.info("LOVMetadataController - updateLOVMetadata()  Started Updating Data, lovMetadata = {}", lovMetadata);
		ApiResponse<LovMetadata> response = null;

		try {		
			var data = lovMetadataService.updateSingle(lovMetadata);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("LOVMetadataController - updateLOVMetadata()  Completed Updating Data, lovMetadata = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("LOVMetadataController - updateLOVMetadata()  Failed to Update Data for lovMetadata = {}, Exception Message : {} ", lovMetadata, e.getMessage());
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
	public ResponseEntity<ApiResponse<List<LovMetadata>>> updateAllLOVMetadata(
			@RequestBody @NotEmpty(message = "Input Lov Metadata cannot be empty.") List<@Valid LovMetadata> lovMetadatas) {
		log.info("LOVMetadataController - updateAllLOVMetadata()  Started Updating Data, lovMetadatas = {}", lovMetadatas);
		ApiResponse<List<LovMetadata>> response = null;
		try {
			var data = lovMetadataService.updateBulk(lovMetadatas);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("LOVMetadataController - updateAllLOVMetadata()  Completed Updating Data, lovMetadatas = {}",data.size());
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("LOVMetadataController - updateAllLOVMetadata()  Failed to Update Data for lovMetadatas = {}, Exception Message : {} ", lovMetadatas, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PatchMapping
	public ResponseEntity<ApiResponse<List<LovMetadata>>> partialupdateLOVMetaData(
			@RequestBody List<Map<String, String>> lovMeatadatas) {
		log.info("LOVMetadataController - partialupdateLOVMetaData()  Started Partial Updating Data, lovMeatadatas = {}", lovMeatadatas);
		ApiResponse<List<LovMetadata>> response = null;
		try {
			var data = lovMetadataService.patch(lovMeatadatas);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("LOVMetadataController - partialupdateLOVMetaData()  Completed Partial Updating Data, lovMeatadatas = {}",data.size());
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("LOVMetadataController - partialupdateLOVMetaData()  Failed to Partial Update Data for lovMeatadatas = {}, Exception Message : {} ", lovMeatadatas, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<String>> deleteLOVMetadataById(@PathVariable("id") long id) {
		log.info("LOVMetadataController - deleteLOVMetadataById()  Started deleting LOV Meata by id = {}",id);
		ApiResponse<String> response = null;

		try {
			lovMetadataService.deleteSingle(id);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Deleted Successfully");
			log.info("LOVMetadataController - deleteLOVMetadataById()  Completed deleting LOV Meata by id = {}",id);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("LOVMetadataController - deleteLOVMetadataById()  Failed to delete LOV Meata for id = {}, Exception Message : {} ", id, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@DeleteMapping("/bulk")
	public ResponseEntity<ApiResponse<String>> deleteAllLOVMetadataById(@RequestBody List<Long> ids) {
		log.info("LOVMetadataController - deleteAllLOVMetadataById()  Started deleting LOV Meata by id's = {}",ids);
		ApiResponse<String> response = null;

		try {
			lovMetadataService.deleteBulk(ids);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Deleted Successfully");
			log.info("LOVMetadataController - deleteAllLOVMetadataById()  Completed deleting LOV Meata by id's = {}",ids);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("LOVMetadataController - deleteAllLOVMetadataById()  Failed to delete LOV Meata for id's = {}, Exception Message : {} ", ids, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/upload")
	public ResponseEntity<ApiResponse<List<LovMetadata>>> addAllLOVMetaDataByFile(@RequestParam("lovfile") MultipartFile lovFile) {
		log.info("LOVMetadataController - addAllLOVMetaDataByFile()  Started Saving Data, lovFile name = {}", lovFile.getOriginalFilename());
		ApiResponse<List<LovMetadata>> response = null;
		try {
			var data = lovMetadataService.saveEntities(lovFile);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("LOVMetadataController - addAllLOVMetaDataByFile()  Completed Saving Data, lovMetadatas = {}",data.size());
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("LOVMetadataController - addAllLOVMetaDataByFile()  Failed to save Data for lovFile name = {}, Exception Message : {} ", lovFile.getOriginalFilename(), e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
		return ResponseEntity.ok().body(response);
	}	
		
	@GetMapping("/basic-info")
	public ResponseEntity<ApiResponse<Map<String, Pagination>>> getAllLovTypeBasicDetails() {
		log.info("LOVMetadataController - getAllLovTypeBasicDetails()  Started Retrieving Data");
		ApiResponse<Map<String,Pagination>> response = null;
		try {
			var data = lovMetadataService.getAllLovTypeBasicDetails();
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("LOVMetadataController - getAllLovTypeBasicDetails()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("LOVMetadataController - getAllLovTypeBasicDetails	()  Failed Retrieving Data. Exception Message : {} ", e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/by/type/all")
	public ResponseEntity<ApiResponse<List<LovMetadata>>> getAllLOVMetadataByType(@RequestParam(name = "type", required = true) String type) {
		log.info("LOVMetadataController - getAllLOVMetadataByType()  Started Retrieving Data of type = {}", type);
		ApiResponse<List<LovMetadata>> response = null;
		try {
			var data = lovMetadataService.findAll(type);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("LOVMetadataController - getAllLOVMetadataByType()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("LOVMetadataController - getAllLOVMetadataByType()  Failed Retrieving Data. Exception Message : {} ", e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/by/type/props/all")
	public ResponseEntity<ApiResponse<Properties>> getAllLOVMetadataPropertiesByType(@RequestParam(name = "type", required = true) String type) {
		//log.info("LOVMetadataController - getAllLOVMetadataPropertiesByType()  Started Retrieving Data of type = {}", type);
		ApiResponse<Properties> response = null;
		try {
			var data = lovMetadataService.findAllProperties(type);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			//log.info("LOVMetadataController - getAllLOVMetadataPropertiesByType()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("LOVMetadataController - getAllLOVMetadataPropertiesByType()  Failed Retrieving Data. Exception Message : {} ", e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/by/type/export")
	public ResponseEntity<Resource> exportData(@RequestParam(name = "type", required = true) String type) {
		log.info("LOVMetadataController - exportData()  Started exporting data of type = {}", type);
		var headers = new HttpHeaders();
		SXSSFWorkbook workbook = null;
		ByteArrayOutputStream baos = null;
		try {
			workbook = lovMetadataService.exportData(type);
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachement", type.strip()+".xlsx");
			baos = new ByteArrayOutputStream();
			workbook.write(baos);
			return ResponseEntity.ok().headers(headers).body(new ByteArrayResource(baos.toByteArray()));
		} catch (Exception e) {
			log.error("LOVMetadataController - exportData() Failed exporting data of type = {}, Exception Message : {} ", type, e.getMessage());
			return ResponseEntity.internalServerError().build();
		} finally {
			log.info("LOVMetadataController - exportData() completed exporting data of type = {}",type);
			try {
				if (workbook != null) {
					workbook.close();
				}
				if (baos != null) {
					baos.close();
				}
			} catch (Exception e) {
				log.error("LOVMetadataController - exportData() Failed to close resources, type = {}, Exception Message : {} ", type, e.getMessage());
			}
		}
	}
	
	@PostMapping("/by/type/pageable")
	public ResponseEntity<ApiResponse<Map<String, Values<LovMetadata>>>> getLovDetailsByTypeAndPage(@RequestParam(name = "type", required = true) String type
																			,@RequestParam(name = "page", required = true) int page) {
		log.info("LOVMetadataController - getLovDetailsByTypeAndPage()  Started Retrieving Data of type = {}, page = {}", type, page);
		ApiResponse<Map<String,Values<LovMetadata>>> response = null;
		try {
			var data = lovMetadataService.findBy(type,page);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("LOVMetadataController - getLovDetailsByTypeAndPage()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("LOVMetadataController - getLovDetailsByTypeAndPage()  Failed Retrieving Data. Exception Message : {} ", e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/by/type/search")
	public ResponseEntity<ApiResponse<Map<String, Values<LovMetadata>>>> getLovByTypeAndNameAndValue(@RequestParam(name = "type", required = true) String type
																							,@RequestParam(name = "name", required = false) String name
																							,@RequestParam(name = "value", required = false) String value) {
		log.info("LOVMetadataController - getLovByTypeAndNameAndValue()  Started Retrieving Data of type = {}, name = {}, value = {}", type, name, value);
		ApiResponse<Map<String,Values<LovMetadata>>> response = null;
		try {
			var data = lovMetadataService.findAll(type, name, value);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("LOVMetadataController - getLovByTypeAndNameAndValue()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("LOVMetadataController - getLovByTypeAndNameAndValue()  Failed Retrieving Data. Exception Message : {} ", e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
}
