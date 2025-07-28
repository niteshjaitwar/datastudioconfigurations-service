package com.adp.esi.digitech.ds.config.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import com.adp.esi.digitech.ds.config.model.TargetDataFormat;
import com.adp.esi.digitech.ds.config.model.TransformationRule;
import com.adp.esi.digitech.ds.config.service.TransformationRulesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;

@Validated
@RestController
@Slf4j
@RequestMapping("/ahub/ds/config/transformation-rule")
@CrossOrigin(origins = "${app.allowed-origins}")
@Tag(name = "Transformation Rules", description = "")
public class TransformationRulesController {
	
	@Autowired
	TransformationRulesService transformationRulesService;

	@Operation(hidden = true)
	@GetMapping
	public ResponseEntity<ApiResponse<List<TransformationRule>>> getAllTransformationRules() {
		log.info("TransformationRulesController - getAllTransformationRules()  Started Retrieving Data");
		ApiResponse<List<TransformationRule>> response = null;
		try {
			var data = transformationRulesService.findAll();
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("TransformationRulesController - getAllTransformationRules()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("TransformationRulesController - getAllTransformationRules()  Failed Retrieving Data. Exception Message : {} ", e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<TransformationRule>> getTransformationRuleById(@PathVariable("id") long id) {
		log.info("TransformationRulesController - getTransformationRuleById()  Started Retrieving Data by id = {}",id);
		ApiResponse<TransformationRule> response = null;
		try {
			TransformationRule data = transformationRulesService.findById(id);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("TransformationRulesController - getTransformationRuleById()  Completed Retrieving Data for id = {}",id);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("TransformationRulesController - getTransformationRuleById()  Failed Retrieving Data for id = {}, Exception Message : {} ", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);

	}
	
	@PostMapping("/by")
	public ResponseEntity<ApiResponse<List<TransformationRule>>> getAllTransformationRulesBy(@RequestParam(name = "bu", required = true) String bu
			  																		,@RequestParam(name = "platform", required = true) String platform
			  																		,@RequestParam(name = "dataCategory", required = true) String dataCategory) {
		log.info("TransformationRulesController - getAllTransformationRulesBy()  Started Retrieving Data By. bu = {}, platform = {}, dataCategory = {}", bu, platform, dataCategory);
		ApiResponse<List<TransformationRule>> response = null;
		try {
			var data = transformationRulesService.findByList(bu, platform, dataCategory);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("TransformationRulesController - getAllTransformationRulesBy()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("TransformationRulesController - getAllTransformationRulesBy()  Failed Retrieving Data. bu = {}, platform = {}, dataCategory = {}, Exception Message : {} ", bu, platform, dataCategory, e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/by/batch")
	@Operation(hidden = true)
	public ResponseEntity<ApiResponse<List<TransformationRule>>> getAllTransformationRulesByForBatch(@RequestParam(name = "bu", required = true) String bu
			  																		,@RequestParam(name = "platform", required = true) String platform
			  																		,@RequestParam(name = "dataCategory", required = true) String dataCategory) {
		log.info("TransformationRulesController - getAllTransformationRulesByForBatch()  Started Retrieving Data By. bu = {}, platform = {}, dataCategory = {}", bu, platform, dataCategory);
		ApiResponse<List<TransformationRule>> response = null;
		try {
			var data = transformationRulesService.findByListForBatch(bu, platform, dataCategory);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("TransformationRulesController - getAllTransformationRulesByForBatch()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("TransformationRulesController - getAllTransformationRulesByForBatch()  Failed Retrieving Data. bu = {}, platform = {}, dataCategory = {}, Exception Message : {} ", bu, platform, dataCategory, e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}

	@PostMapping
	public ResponseEntity<ApiResponse<TransformationRule>> addTransformationRule(@Valid @RequestBody TransformationRule transformationRule) {
		
		log.info("TransformationRulesController - addTransformationRule()  Started Saving Data, transformationRule = {}", transformationRule);
		ApiResponse<TransformationRule> response = null;

		try {
			var data = transformationRulesService.saveEntity(transformationRule);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("TransformationRulesController - addTransformationRule()  Completed Saving Data, transformationRule = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("TransformationRulesController - addTransformationRule()  Failed to save Data for transformationRule = {}, Exception Message : {} ", transformationRule, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/bulk")
	public ResponseEntity<ApiResponse<List<TransformationRule>>> addTransformationRules(
			@RequestBody @NotEmpty(message = "Input Transformation Rules cannot be empty.") List<@Valid TransformationRule> transformationRules) {
		log.info("TransformationRulesController - addTransformationRules()  Started Saving Data, transformationRules = {}", transformationRules);
		ApiResponse<List<TransformationRule>> response = null;

		try {
			var data = transformationRulesService.saveEntities(transformationRules);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("TransformationRulesController - addTransformationRules()  Completed Saving Data, transformationRules = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("TransformationRulesController - addTransformationRules()  Failed to save Data for transformationRules = {}, Exception Message : {} ", transformationRules, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PutMapping
	public ResponseEntity<ApiResponse<TransformationRule>> updateTransformationRule(
			@Valid @RequestBody TransformationRule transformationRule) {
		log.info("TransformationRulesController - updateTransformationRule()  Started Updating Data, transformationRule = {}", transformationRule);
		ApiResponse<TransformationRule> response = null;

		try {		
			var data = transformationRulesService.updateSingle(transformationRule);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("TransformationRulesController - updateTransformationRule()  Completed Updating Data, transformationRule = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("TransformationRulesController - updateTransformationRule()  Failed to Update Data for transformationRule = {}, Exception Message : {} ", transformationRule, e.getMessage());
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
	public ResponseEntity<ApiResponse<List<TransformationRule>>> updateTransformationRules(
			@RequestBody @NotEmpty(message = "Input Transformation Rules cannot be empty.") List<@Valid TransformationRule> transformationRules) {
		log.info("TransformationRulesController - updateTransformationRules()  Started Updating Data, transformationRules = {}", transformationRules);
		ApiResponse<List<TransformationRule>> response = null;
		try {
			var data = transformationRulesService.updateBulk(transformationRules);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("TransformationRulesController - updateTransformationRules()  Completed Updating Data, transformationRules = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("TransformationRulesController - updateTransformationRules()  Failed to Update Data for transformationRules = {}, Exception Message : {} ", transformationRules, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PatchMapping
	public ResponseEntity<ApiResponse<List<TransformationRule>>> partialupdateTransformationRules(
			@RequestBody List<Map<String, String>> transformationRules) {
		log.info("TransformationRulesController - partialupdateTransformationRules()  Started Partial Updating Data, transformationRules = {}", transformationRules);
		ApiResponse<List<TransformationRule>> response = null;
		try {
			var data = transformationRulesService.patch(transformationRules);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("TransformationRulesController - partialupdateTransformationRules()  Completed Partial Updating Data, transformationRules = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("TransformationRulesController - partialupdateTransformationRules()  Failed to Partial Update Data for transformationRules = {}, Exception Message : {} ", transformationRules, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<String>> deleteTransformationRuleById(@PathVariable("id") long id) {
		log.info("TransformationRulesController - deleteTransformationRuleById()  Started deleting transformation rule by id = {}",id);
		ApiResponse<String> response = null;

		try {
			transformationRulesService.deleteSingle(id);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Deleted Successfully");
			log.info("TransformationRulesController - deleteTransformationRuleById()  Completed deleting transformation rule by id = {}",id);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("TransformationRulesController - deleteTransformationRuleById()  Failed to delete transformation rule for id = {}, Exception Message : {} ", id, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@DeleteMapping("/bulk")
	public ResponseEntity<ApiResponse<String>> deleteTransformationRulesById(@RequestBody List<Long> ids) {
		log.info("TransformationRulesController - deleteTransformationRulesById()  Started deleting transformation rule by id's = {}",ids);
		ApiResponse<String> response = null;

		try {
			transformationRulesService.deleteBulk(ids);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Deleted Successfully");
			log.info("TransformationRulesController - deleteTransformationRulesById()  Completed deleting transformation rule by id's = {}",ids);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("TransformationRulesController - deleteTransformationRulesById()  Failed to delete transformation rule for id's = {}, Exception Message : {} ", ids, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/target-data-format/by")
	public ResponseEntity<ApiResponse<Map<UUID, TargetDataFormat>>> getTargetFormatMap(@RequestParam(name = "bu", required = true) String bu
				,@RequestParam(name = "platform", required = true) String platform
				,@RequestParam(name = "dataCategory", required = true) String dataCategory
				,@RequestParam(name = "dataSetId", required = true) String globalDataSetUuid) {
		log.info("TransformationRulesController - getTargetFormatMap() Started getting Data TargetFormat Map");
		ApiResponse<Map<UUID, TargetDataFormat>> response = null;

		try {
			var data = transformationRulesService.getTargetFormatMap(bu, platform, dataCategory, globalDataSetUuid);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("TransformationRulesController - addTransformationRules()  Completed getting Data TargetFormat Map");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("TransformationRulesController - getTargetFormatMap()  Failed getting Data TargetFormat Map");
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
}
