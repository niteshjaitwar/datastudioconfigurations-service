package com.adp.esi.digitech.ds.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FilePollingConfig {

	private Long id;

	private String bu;

	private String location;

	private String appCode;

	private String schedular;

	@JsonProperty("config")
	private String config;
	
	@NotEmpty(message = "The useremail is Required")
	private String useremail;
	
	@NotEmpty(message = "The userrole is Required")
	private String userrole;

}
