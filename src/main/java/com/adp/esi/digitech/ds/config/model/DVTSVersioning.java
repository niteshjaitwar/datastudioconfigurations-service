package com.adp.esi.digitech.ds.config.model;

import jakarta.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class DVTSVersioning {

	private Long id;
	
	@NotEmpty(message = "The bu is Required")
	private String bu;
		
	@NotEmpty(message = "The platform is Required")
	private String platform;
	
	@NotEmpty(message = "The dataCategory is Required")
	private String dataCategory;
	
	//@NotEmpty(message = "The versionName is Required")
	private String versionName;
	
	private String description;
	
	@JsonIgnore
	private String dvtsConfigData;
	
	@JsonIgnore
	private String encryptedConfigData;
	
	@NotEmpty(message = "The userrole is Required")
	private String userrole;
	
	@NotEmpty(message = "The useremail is Required")
	private String useremail;
	
	private String createdDateTime;	
	
}
