package com.adp.esi.digitech.ds.config.model;

import jakarta.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class LovMetadata {

	private Long id;
	
	@NotEmpty(message = "The lovType is Required")
	private String lovType;
	
	@NotEmpty(message = "The lovName is Required")
	private String lovName;
	
	@NotEmpty(message = "The lovValue is Required")
	private String lovValue;
}
