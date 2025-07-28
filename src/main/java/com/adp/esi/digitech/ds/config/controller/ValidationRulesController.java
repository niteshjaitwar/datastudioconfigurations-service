package com.adp.esi.digitech.ds.config.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.adp.esi.digitech.ds.config.model.ValidationRule;
import com.adp.esi.digitech.ds.config.service.ValidationRulesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;

@Validated
@RestController
@Slf4j
@RequestMapping("/ahub/ds/config/validation-rule")
@CrossOrigin(origins = "${app.allowed-origins}")
@Tag(name = "Validation Rules", description = "")
public class ValidationRulesController {

	@Autowired
	ValidationRulesService validationService;

	@Operation(hidden = true)
	@GetMapping
	public ResponseEntity<ApiResponse<List<ValidationRule>>> getAllValidationRules() {
		log.info("ValidationRulesController - getAllValidationRules()  Started Retrieving Data");
		ApiResponse<List<ValidationRule>> response = null;
		try {
			var data = validationService.findAll();
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ValidationRulesController - getAllValidationRules()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ValidationRulesController - getAllValidationRules()  Failed Retrieving Data. Exception Message : {} ", e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ValidationRule>> getValidationRuleById(@PathVariable("id") long id) {
		log.info("ValidationRulesController - getValidationRuleById()  Started Retrieving Data by id = {}",id);
		ApiResponse<ValidationRule> response = null;		
		try {			
			var data = validationService.findById(id);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ValidationRulesController - getValidationRuleById()  Completed Retrieving Data for id = {}",id);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ValidationRulesController - getValidationRuleById()  Failed Retrieving Data for id = {}, Exception Message : {} ", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}
		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/by")
	public ResponseEntity<ApiResponse<List<ValidationRule>>> getAllValidationRulesBy(@RequestParam(name = "bu", required = true) String bu
			  																		,@RequestParam(name = "platform", required = true) String platform
			  																		,@RequestParam(name = "dataCategory", required = true) String dataCategory) {
		log.info("ValidationRulesController - getAllValidationRulesBy()  Started Retrieving Data By. bu = {}, platform = {}, dataCategory = {}", bu, platform, dataCategory);
		ApiResponse<List<ValidationRule>> response = null;
		try {
			var data = validationService.findBy(bu, platform, dataCategory).values().stream().flatMap(List::stream).collect(Collectors.toList());
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ValidationRulesController - getAllValidationRulesBy()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ValidationRulesController - getAllValidationRulesBy()  Failed Retrieving Data. bu = {}, platform = {}, dataCategory = {}, Exception Message : {} ", bu, platform, dataCategory, e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/by/type")
	public ResponseEntity<ApiResponse<List<ValidationRule>>> getAllValidationRulesByType(@RequestParam(name = "bu", required = true) String bu
			  																		,@RequestParam(name = "platform", required = true) String platform
			  																		,@RequestParam(name = "dataCategory", required = true) String dataCategory
			  																		,@RequestParam(name = "ruleType", required = true) String ruleType) {
		log.info("ValidationRulesController - getAllValidationRulesByType()  Started Retrieving Data By. bu = {}, platform = {}, dataCategory = {}, ruleType = {}", bu, platform, dataCategory, ruleType);
		ApiResponse<List<ValidationRule>> response = null;
		try {
			var data = validationService.findByList(bu, platform, dataCategory, ruleType);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ValidationRulesController - getAllValidationRulesByType()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ValidationRulesController - getAllValidationRulesByType()  Failed Retrieving Data. bu = {}, platform = {}, dataCategory = {}, ruleType = {}, Exception Message : {} ", bu, platform, dataCategory, ruleType, e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/by/type/batch")
	public ResponseEntity<ApiResponse<List<ValidationRule>>> getAllValidationRulesByTypeForBatch(@RequestParam(name = "bu", required = true) String bu
			  																		,@RequestParam(name = "platform", required = true) String platform
			  																		,@RequestParam(name = "dataCategory", required = true) String dataCategory
			  																		,@RequestParam(name = "ruleType", required = true) String ruleType) {
		log.info("ValidationRulesController - getAllValidationRulesByTypeForBatch()  Started Retrieving Data By. bu = {}, platform = {}, dataCategory = {}, ruleType = {}", bu, platform, dataCategory, ruleType);
		ApiResponse<List<ValidationRule>> response = null;
		try {
			var data = validationService.findByListForBatch(bu, platform, dataCategory, ruleType);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ValidationRulesController - getAllValidationRulesByTypeForBatch()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ValidationRulesController - getAllValidationRulesByTypeForBatch()  Failed Retrieving Data. bu = {}, platform = {}, dataCategory = {}, ruleType = {}, Exception Message : {} ", bu, platform, dataCategory, ruleType, e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}

	@PostMapping
	public ResponseEntity<ApiResponse<ValidationRule>> addValidationRule(@Valid	@RequestBody ValidationRule validationRule) {
		log.info("ValidationRulesController - addValidationRule()  Started Saving Data, validationRule = {}", validationRule);
		ApiResponse<ValidationRule> response = null;

		try {
			var data = validationService.saveEntity(validationRule);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ValidationRulesController - addValidationRule()  Completed Saving Data, validationRule = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ValidationRulesController - addValidationRule()  Failed to save Data for validationRule = {}, Exception Message : {} ", validationRule, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/bulk")
	public ResponseEntity<ApiResponse<List<ValidationRule>>> addValidationRules(
			@RequestBody @NotEmpty(message = "Input Validation Rules cannot be empty.")List<@Valid ValidationRule> validationRuleEntities) {
		log.info("ValidationRulesController - addValidationRules()  Started Saving Data, validationRules = {}", validationRuleEntities);
		ApiResponse<List<ValidationRule>> response = null;

		try {
			var data = validationService.saveEntities(validationRuleEntities);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ValidationRulesController - addValidationRules()  Completed Saving Data, validationRules = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ValidationRulesController - addValidationRules()  Failed to save Data for validationRules = {}, Exception Message : {} ", validationRuleEntities, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}

	@PutMapping
	public ResponseEntity<ApiResponse<ValidationRule>> updateValidationRule(
			@Valid @RequestBody ValidationRule validationRule) {
		log.info("ValidationRulesController - updateValidationRule()  Started Updating Data, validationRule = {}", validationRule);
		ApiResponse<ValidationRule> response = null;

		try {		
			var data = validationService.updateSingle(validationRule);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ValidationRulesController - updateValidationRule()  Completed Updating Data, validationRule = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ValidationRulesController - updateValidationRule()  Failed to Update Data for validationRule = {}, Exception Message : {} ", validationRule, e.getMessage());
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
	public ResponseEntity<ApiResponse<List<ValidationRule>>> updateValidationRules(
			@RequestBody @NotEmpty(message = "Input Validation Rules cannot be empty.") List<@Valid ValidationRule> validationRules) {
		log.info("ValidationRulesController - updateValidationRules()  Started Updating Data, validationRules = {}", validationRules);
		ApiResponse<List<ValidationRule>> response = null;
		try {
			var data = validationService.updateBulk(validationRules);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ValidationRulesController - updateValidationRules()  Completed Updating Data, validationRules = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ValidationRulesController - updateValidationRules()  Failed to Update Data for validationRules = {}, Exception Message : {} ", validationRules, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}

	@PatchMapping
	public ResponseEntity<ApiResponse<List<ValidationRule>>> partialupdateValidationRules(
			@RequestBody List<Map<String, String>> validationRules) {
		log.info("ValidationRulesController - partialupdateValidationRules()  Started Partial Updating Data, validationRules = {}", validationRules);
		ApiResponse<List<ValidationRule>> response = null;
		try {
			var data = validationService.patch(validationRules);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ValidationRulesController - partialupdateValidationRules()  Completed Partial Updating Data, validationRules = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ValidationRulesController - partialupdateValidationRules()  Failed to Partial Update Data for validationRules = {}, Exception Message : {} ", validationRules, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<String>> deleteValidationRuleById(@PathVariable("id") long id) {
		log.info("ValidationRulesController - deleteValidationRuleById()  Started deleting validation rule by id = {}",id);
		ApiResponse<String> response = null;

		try {
			validationService.deleteSingle(id);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Deleted Successfully");
			log.info("ValidationRulesController - deleteValidationRuleById()  Completed deleting validation rule by id = {}",id);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ValidationRulesController - deleteValidationRuleById()  Failed to delete validation rule for id = {}, Exception Message : {} ", id, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}

	@DeleteMapping("/bulk")
	public ResponseEntity<ApiResponse<String>> deleteValidationRulesById(@RequestBody List<Long> ids) {
		log.info("ValidationRulesController - deleteValidationRulesById()  Started deleting validation rule by id's = {}",ids);
		ApiResponse<String> response = null;

		try {
			validationService.deleteBulk(ids);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Deleted Successfully");
			log.info("ValidationRulesController - deleteValidationRulesById()  Completed deleting validation rule by id's = {}",ids);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ValidationRulesController - deleteValidationRulesById()  Failed to delete validation rule for id's = {}, Exception Message : {} ", ids, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}

}
