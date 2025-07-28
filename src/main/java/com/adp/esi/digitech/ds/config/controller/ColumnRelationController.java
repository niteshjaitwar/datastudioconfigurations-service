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
import com.adp.esi.digitech.ds.config.model.ColumnRelation;
import com.adp.esi.digitech.ds.config.model.ErrorResponse;
import com.adp.esi.digitech.ds.config.service.ColumnRelationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
@Validated
@Slf4j
@RestController
@RequestMapping("/ahub/ds/config/column-relation")
@CrossOrigin(origins = "${app.allowed-origins}")
@Tag(name = "Column Relation", description = "")
public class ColumnRelationController {
	
	@Autowired
	ColumnRelationService columnRelationService;
	
	@Operation(hidden = true)
	@GetMapping
	public ResponseEntity<ApiResponse<List<ColumnRelation>>> getAllColumnRelations() {
		log.info("ColumnRelationController - getAllColumnRelations()  Started Retrieving Data");
		ApiResponse<List<ColumnRelation>> response = null;
		try {
			var data = columnRelationService.findAll();
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ColumnRelationController - getAllColumnRelations()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnRelationController - getAllColumnRelations()  Failed Retrieving Data. Exception Message : {} ", e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/by")
	public ResponseEntity<ApiResponse<List<ColumnRelation>>> getAllColumnRelationsBy(@RequestParam(name = "bu", required = true) String bu
			  																		,@RequestParam(name = "platform", required = true) String platform
			  																		,@RequestParam(name = "dataCategory", required = true) String dataCategory) {
		log.info("ColumnRelationController - getAllColumnRelationsBy()  Started Retrieving Data By. bu = {}, platform = {}, dataCategory = {}", bu, platform, dataCategory);
		ApiResponse<List<ColumnRelation>> response = null;
		try {
			var data = columnRelationService.findByList(bu, platform, dataCategory);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ColumnRelationController - getAllColumnRelationsBy()  Completed Retrieving Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnRelationController - getAllColumnRelationsBy()  Failed Retrieving Data. bu = {}, platform = {}, dataCategory = {}, Exception Message : {} ", bu, platform, dataCategory, e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ColumnRelation>> getColumnRelationById(@PathVariable("id") long id) {
		log.info("ColumnRelationController - getColumnRelationById()  Started Retrieving Data by id = {}",id);
		ApiResponse<ColumnRelation> response = null;		
		try {			
			var data = columnRelationService.findById(id);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ColumnRelationController - getColumnRelationById()  Completed Retrieving Data for id = {}",id);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("204", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnRelationController - getColumnRelationById()  Failed Retrieving Data for id = {}, Exception Message : {} ", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}
		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping
	public ResponseEntity<ApiResponse<ColumnRelation>> addColumnRelation(@Valid	@RequestBody ColumnRelation columnRelation) {
		log.info("ColumnRelationController - addColumnRelation()  Started Saving Data, columnRelation = {}", columnRelation);
		ApiResponse<ColumnRelation> response = null;

		try {
			var data = columnRelationService.saveEntity(columnRelation);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ColumnRelationController - addColumnRelation()  Completed Saving Data, columnRelation = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnRelationController - addColumnRelation()  Failed to save Data for columnRelation = {}, Exception Message : {} ", columnRelation, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/bulk")
	public ResponseEntity<ApiResponse<List<ColumnRelation>>> addColumnRelations(
			@RequestBody @NotEmpty(message = "Input Column Relations cannot be empty.")List<@Valid ColumnRelation> columnRelationEntities) {
		log.info("ColumnRelationController - addColumnRelations()  Started Saving Data, columnRelations = {}", columnRelationEntities);
		ApiResponse<List<ColumnRelation>> response = null;

		try {
			var data = columnRelationService.saveEntities(columnRelationEntities);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ColumnRelationController - addColumnRelations()  Completed Saving Data, columnRelations = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnRelationController - addColumnRelations()  Failed to save Data for columnRelations = {}, Exception Message : {} ", columnRelationEntities, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PutMapping
	public ResponseEntity<ApiResponse<ColumnRelation>> updateColumnRelation(
			@Valid @RequestBody ColumnRelation columnRelation) {
		log.info("ColumnRelationController - updateColumnRelation()  Started Updating Data, columnRelation = {}", columnRelation);
		ApiResponse<ColumnRelation> response = null;

		try {		
			var data = columnRelationService.updateSingle(columnRelation);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ColumnRelationController - updateColumnRelation()  Completed Updating Data, columnRelation = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnRelationController - updateColumnRelation()  Failed to Update Data for columnRelation = {}, Exception Message : {} ", columnRelation, e.getMessage());
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
	public ResponseEntity<ApiResponse<List<ColumnRelation>>> updateColumnRelations(
			@RequestBody @NotEmpty(message = "Input Column Relations cannot be empty.") List<@Valid ColumnRelation> columnRelations) {
		log.info("ColumnRelationController - updateColumnRelations()  Started Updating Data, columnRelations = {}", columnRelations);
		ApiResponse<List<ColumnRelation>> response = null;
		try {
			var data = columnRelationService.updateBulk(columnRelations);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ColumnRelationController - updateColumnRelations()  Completed Updating Data, columnRelations = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnRelationController - updateColumnRelations()  Failed to Update Data for columnRelations = {}, Exception Message : {} ", columnRelations, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@PatchMapping
	public ResponseEntity<ApiResponse<List<ColumnRelation>>> partialupdateColumnRelations(
			@RequestBody List<Map<String, String>> columnRelations) {
		log.info("ColumnRelationController - partialupdateColumnRelations()  Started Partial Updating Data, columnRelations = {}", columnRelations);
		ApiResponse<List<ColumnRelation>> response = null;
		try {
			var data = columnRelationService.patch(columnRelations);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, data);
			log.info("ColumnRelationController - partialupdateColumnRelations()  Completed Partial Updating Data, columnRelations = {}",data);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnRelationController - partialupdateColumnRelations()  Failed to Partial Update Data for columnRelations = {}, Exception Message : {} ", columnRelations, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<String>> deleteColumnRelationById(@PathVariable("id") long id) {
		log.info("ColumnRelationController - deleteColumnRelationById()  Started deleting Column Relation by id = {}",id);
		ApiResponse<String> response = null;

		try {
			columnRelationService.deleteSingle(id);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Deleted Successfully");
			log.info("ColumnRelationController - deleteColumnRelationById()  Completed deleting Column Relation by id = {}",id);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnRelationController - deleteColumnRelationById()  Failed to delete Column Relation for id = {}, Exception Message : {} ", id, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@DeleteMapping("/bulk")
	public ResponseEntity<ApiResponse<String>> deleteColumnRelationsById(@RequestBody List<Long> ids) {
		log.info("ColumnRelationController - deleteColumnRelationsById()  Started deleting Column Relation by id's = {}",ids);
		ApiResponse<String> response = null;

		try {
			columnRelationService.deleteBulk(ids);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Deleted Successfully");
			log.info("ColumnRelationController - deleteColumnRelationsById()  Completed deleting Column Relation by id's = {}",ids);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("ColumnRelationController - deleteColumnRelationsById()  Failed to delete Column Relation for id's = {}, Exception Message : {} ", ids, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}

}
