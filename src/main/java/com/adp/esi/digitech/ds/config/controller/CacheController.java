package com.adp.esi.digitech.ds.config.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adp.esi.digitech.ds.config.model.ApiResponse;
import com.adp.esi.digitech.ds.config.model.ErrorResponse;
import com.adp.esi.digitech.ds.config.service.CacheService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
@Validated
@Slf4j
@RestController
@RequestMapping("/ahub/ds/config/cache")
@CrossOrigin(origins = "${app.allowed-origins}")
@Tag(name = "Cache Data", description = "")
public class CacheController {
	
	@Autowired
	CacheService cacheService;
	
	@Operation(hidden = true)
	@PostMapping("/evict")
	public ResponseEntity<ApiResponse<String>> evictCache(@RequestParam(name = "cacheName", required = true) String cacheName, @RequestParam(name = "key",  required = false) String key) {
		log.info("CacheController - evictCaches() Started evicting caches by cacheName = {}", cacheName);
		ApiResponse<String> response = null;
		try {
			cacheService.evictCache(cacheName, key);
			response = ApiResponse.success(ApiResponse.Status.SUCCESS, "Evicted Successfully");
			log.info("CacheController - evictCaches() Completed evicting Data");
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse("500", e.getMessage());
			response = ApiResponse.error(ApiResponse.Status.ERROR, error);
			log.error("CacheController - evictCaches() Failed evicting caches by cacheName = {}, key = {}, Exception Message : {} ", cacheName, key, e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
		return ResponseEntity.ok().body(response);
	}
	
}
