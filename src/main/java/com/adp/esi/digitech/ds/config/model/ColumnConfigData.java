package com.adp.esi.digitech.ds.config.model;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern.Flag;
import lombok.Data;

@Data
public class ColumnConfigData {
	
	@NotEmpty(message = "The bu is Required")
	private String bu;
		
	@NotEmpty(message = "The platform is Required")
	private String platform;
	
	@NotEmpty(message = "The dataCategory is Required")
	private String dataCategory;
	
	@NotEmpty(message = "The UUIDs are Required")
	private List<String> uuids;
		
	@NotEmpty(message = "The useremail is Required")
	@Email(message = "The useremail is invalid", flags = {Flag.CASE_INSENSITIVE})
	private String useremail;
	
	@NotEmpty(message = "The userrole is Required")
	private String userrole;
	


}
