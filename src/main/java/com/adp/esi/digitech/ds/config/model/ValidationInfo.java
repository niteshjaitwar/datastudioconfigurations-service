package com.adp.esi.digitech.ds.config.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class ValidationInfo {
	public final String field;
	public final List<String> validations;
	
	@JsonCreator
	public ValidationInfo(@JsonProperty("field") String field, @JsonProperty("validations") List<String> validations) {
		this.field = field;
		this.validations = validations;
	}
}
