package com.adp.esi.digitech.ds.config.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ColumnConfigurationResponse {
	
	@JsonProperty("configuration-data")
	ConfigurationData configurationData;
	
	@JsonProperty("column-relations")
	List<ColumnRelation> columnRelations;
	
	@JsonProperty("validation-rules")
	List<ValidationRule> validationRules;
	
	@JsonProperty("transformation-rules")
	List<TransformationRule> transformationRules;
	
}
