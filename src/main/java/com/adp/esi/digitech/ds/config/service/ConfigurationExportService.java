package com.adp.esi.digitech.ds.config.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adp.esi.digitech.ds.config.model.DVTSVersioning;
import com.adp.esi.digitech.ds.config.util.EncryptionUtils;

@Service
public class ConfigurationExportService {
	
	@Autowired
	DVTSVersioningService dvtsVersioningService;
	
	@Autowired
	EncryptionUtils encryptionUtils;
	
	public DVTSVersioning exportData(Long id) throws Exception {
		DVTSVersioning dvtsVersioning = dvtsVersioningService.findById(id);
		
		JSONObject obj = new JSONObject(dvtsVersioning.getDvtsConfigData());
		var encryptedString = encryptionUtils.encrypt(obj.toString());
		dvtsVersioning.setEncryptedConfigData(encryptedString);
		return dvtsVersioning;
	}
}
