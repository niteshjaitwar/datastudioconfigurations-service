package com.adp.esi.digitech.ds.config.util;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidationUtil {
	
	private static final String DATASET_ID = "dataSetId"; 
	private static final String CONFIGURATIONS = "configurations";
	
	public static boolean isHavingValue(String value) {
		return (Objects.nonNull(value) && !value.isBlank() && !value.isEmpty());
	}	
	
	public static boolean isValidJsonArray(String value) {
		if(ValidationUtil.isHavingValue(value)) {
			try {
				new JSONArray(value);
				return true;
			} catch (JSONException e) {
				log.error("ValidationUtil - isValidJsonArray, Json Parsing failed message = {}", e.getMessage());
				return false;
			}
		}
		
		return false;
	}
	
	public static boolean isContainsDatasetRules(String rules, String globalDataSetUuid) {
		return ValidationUtil.isHavingValue(rules) && rules.contains(globalDataSetUuid);
	}
	
	// This Method Handles DATA_TRANSFORMATION_RULES, DATA_EXCLUSION_RULES and REMOVE_SPECIAL_CHAR
		public static JSONObject getDatasetRules(String rules, UUID globalDataSetUuid) {
			if(globalDataSetUuid == null || !ValidationUtil.isHavingValue(rules))
				return null;
			
			JSONArray dataSetArrayRules = new JSONArray(rules);
			
			OptionalInt selectedOptionalIndex  = IntStream.range(0, dataSetArrayRules.length()).parallel().filter(index -> {
				
				JSONObject dataSetRule = dataSetArrayRules.getJSONObject(index);
				if(dataSetRule != null && dataSetRule.has(ValidationUtil.DATASET_ID) && !dataSetRule.isNull(ValidationUtil.DATASET_ID)) {
					UUID currentDataSetUuid = UUID.fromString(dataSetRule.getString(ValidationUtil.DATASET_ID));									
					return globalDataSetUuid.equals(currentDataSetUuid);
				}								
				return false;
			}).findFirst();
			
			if(selectedOptionalIndex.isEmpty())
				return null;
			
			int selectedIndex = selectedOptionalIndex.getAsInt();
			var selectedDataSetRules = dataSetArrayRules.getJSONObject(selectedIndex);
			
			if(!selectedDataSetRules.has(ValidationUtil.CONFIGURATIONS) || selectedDataSetRules.isNull(ValidationUtil.CONFIGURATIONS))
				return null;
			
			return selectedDataSetRules.getJSONObject(ValidationUtil.CONFIGURATIONS);
		}
}
