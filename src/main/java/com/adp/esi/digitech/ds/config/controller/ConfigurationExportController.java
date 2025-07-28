package com.adp.esi.digitech.ds.config.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adp.esi.digitech.ds.config.service.ConfigurationExportService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Validated
@RestController
@RequestMapping("/ahub/ds/config/export")
@CrossOrigin(origins = "${app.allowed-origins}")
@Tag(name = "Configuration Export", description = "")
public class ConfigurationExportController {

	@Autowired
	private ConfigurationExportService configurationExportService;
	
	

	@GetMapping("/{id}")
	public ResponseEntity<Resource> exportData(@PathVariable("id") Long id) {
		log.info("ConfigurationExportController - exportData()  Started exporting data of id = {}", id);
		var headers = new HttpHeaders();
		try {
			var dvtsVersioning = configurationExportService.exportData(id);
			var fileName = dvtsVersioning.getId() + "_" +dvtsVersioning.getBu()+"_"+dvtsVersioning.getPlatform()+"_"+dvtsVersioning.getDataCategory()+"_"+dvtsVersioning.getVersionName();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachement", fileName.strip()+".txt");
			log.info(
					"ConfigurationExportController - exportData() completed exporting data of id = {}, bu = {}, platform = {}, dataCategory = {}",
					dvtsVersioning.getId(), dvtsVersioning.getBu(), dvtsVersioning.getPlatform(),
					dvtsVersioning.getDataCategory());
			
			return ResponseEntity.ok().headers(headers).body(new ByteArrayResource(dvtsVersioning.getEncryptedConfigData().getBytes()));
		} catch (Exception e) {
			//TODO global exception has to be thrown here
			log.error("ConfigurationExportController - exportData() Failed exporting data of id = {}, Exception Message : {} ",
					id, e.getMessage());
			return ResponseEntity.internalServerError().build();
		}
	}

}
