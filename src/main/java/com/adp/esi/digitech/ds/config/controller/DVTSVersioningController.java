package com.adp.esi.digitech.ds.config.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adp.esi.digitech.ds.config.model.ApiResponse;
import com.adp.esi.digitech.ds.config.model.DVTSVersioning;
import com.adp.esi.digitech.ds.config.model.ErrorResponse;
import com.adp.esi.digitech.ds.config.service.DVTSVersioningService;
import com.adp.esi.digitech.ds.config.util.ValidationUtil;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Validated
@Slf4j
@RestController
@RequestMapping("/ahub/ds/config/version")
@CrossOrigin(origins = "${app.allowed-origins}")
@Tag(name = "Configuration Versioning", description = "")
public class DVTSVersioningController {
	
	@Autowired
	DVTSVersioningService dvtsVersioningService;
	
	@PostMapping
	public ResponseEntity<ApiResponse<DVTSVersioning>> createVersion(@Valid @RequestBody DVTSVersioning versioning) {
		log.info("DVTSVersioningController - createVersion() started create versioning bu = {}, platform = {}, dataCategory = {}, versionName = {}", versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory(),versioning.getVersionName());
		ApiResponse<DVTSVersioning> response = null;
		try {
			if(!ValidationUtil.isHavingValue(versioning.getVersionName())) {
				ErrorResponse error = new ErrorResponse("400", "version name is mandatory");
				response = ApiResponse.error(ApiResponse.Status.ERROR, error);
				log.error("DVTSVersioningController - createVersion()  Failed create versioning. bu = {}, platform = {}, dataCategory = {}, versionName = {}, Exception Message : {} ", versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory(), versioning.getVersionName(), "version name is mandatory");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
			var temp = dvtsVersioningService.createVersion(versioning);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, temp);
			log.info("DVTSVersioningController - createVersion() completed create versioning versioning bu = {}, platform = {}, dataCategory = {}, versionName = {}", versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory(), versioning.getVersionName());
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("DVTSVersioningController - createVersion()  Failed create versioning. bu = {}, platform = {}, dataCategory = {}, versionName = {}, Exception Message : {} ", versioning.getBu(), versioning.getPlatform(), versioning.getDataCategory(), versioning.getVersionName(), e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("/by")
	public ResponseEntity<ApiResponse<List<DVTSVersioning>>> getVersioningData(@RequestParam(name = "bu", required = true) String bu
				,@RequestParam(name = "platform", required = true) String platform
				,@RequestParam(name = "dataCategory", required = true) String dataCategory) {
		log.info("DVTSVersioningController - getVersioningData() started get versioning data bu = {}, platform = {}, dataCategory = {}", bu, platform, dataCategory);
		ApiResponse<List<DVTSVersioning>> response = null;
		try {
			var dvtsVersioning = dvtsVersioningService.findBy(bu, platform, dataCategory);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, dvtsVersioning);
			log.info("DVTSVersioningController - getVersioningData() completed get versioning data bu = {}, platform = {}, dataCategory = {}", bu, platform, dataCategory);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("DVTSVersioningController - getVersioningData()  Failed get versioning data. data bu = {}, platform = {}, dataCategory = {}, Exception Message : {} ", bu, platform, dataCategory, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
		return ResponseEntity.ok().body(response);
	}
	
	@PatchMapping("/revert/{id}")
	public ResponseEntity<ApiResponse<String>> revert(@PathVariable("id") Long id,@RequestParam(name = "userrole", required = true) String userrole, @RequestParam(name = "useremail", required = true) String useremail) {
		log.info("DVTSVersioningController - revert()  Started reverting data of id = {}", id);
		ApiResponse<String> response = null;
		try {
			dvtsVersioningService.revert(id, userrole, useremail);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, id+" reverted successfully.");
			log.info("DVTSVersioningController - revert() completed reverting of id = {}", id);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("DVTSVersioningController - revert()  Failed reverting of id = {}", id);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
		return ResponseEntity.ok().body(response);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<String>> deleteById(@PathVariable("id") long id) {
		log.info("DVTSVersioningController - deleteById()  Started deleting DVTSVersioning by id = {}",id);
		ApiResponse<String> response = null;

		try {
			dvtsVersioningService.deleteSingle(id);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Deleted Successfully");
			log.info("DVTSVersioningController - deleteById()  Completed deleting DVTSVersioning by id = {}",id);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("DVTSVersioningController - deleteById()  Failed to delete DVTSVersioning for id = {}, Exception Message : {} ", id, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}

	@DeleteMapping("/bulk")
	public ResponseEntity<ApiResponse<String>> deleteByIds(@RequestBody List<Long> ids) {
		log.info("DVTSVersioningController - deleteByIds()  Started deleting DVTSVersioning by id's = {}", ids);
		ApiResponse<String> response = null;

		try {
			dvtsVersioningService.deleteBulk(ids);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Deleted Successfully");
			log.info("DVTSVersioningController - deleteByIds()  Completed deleting DVTSVersioning by id's = {}",ids);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("DVTSVersioningController - deleteByIds()  Failed to delete DVTSVersioning for id's = {}, Exception Message : {} ", ids, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
	
	@DeleteMapping("/by")
	public ResponseEntity<ApiResponse<String>> deleteBy(@RequestParam(name = "bu", required = true) String bu
			,@RequestParam(name = "platform", required = true) String platform
			,@RequestParam(name = "dataCategory", required = true) String dataCategory) {
		log.info("DVTSVersioningController - deleteBy()  Started deleting DVTSVersioning by bu = {}, platform = {}, dataCategory = {}", bu, platform, dataCategory);
		ApiResponse<String> response = null;

		try {
			dvtsVersioningService.deleteBy(bu, platform, dataCategory);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Deleted Successfully");
			log.info("DVTSVersioningController - deleteBy()  Completed deleting DVTSVersioning by bu = {}, platform = {}, dataCategory = {}", bu, platform, dataCategory);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("DVTSVersioningController - deleteBy()  Failed to delete DVTSVersioning for bu = {}, platform = {}, dataCategory = {}, Exception Message : {} ", bu, platform, dataCategory, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}

		return ResponseEntity.ok().body(response);
	}
}
