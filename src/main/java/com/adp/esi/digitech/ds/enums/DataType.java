package com.adp.esi.digitech.ds.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DataType {
	
	Text("Text"), 
	Number("Number"), 
	Date("Date"),
	Dropdown("Dropdown"), 
	Email("Email"),
	Regex("Regex");
	
	private final String dataType;
	
	DataType(String dataType) {
		this.dataType = dataType;
	}
	
	@JsonValue
	public String getDataType() {
		return dataType;
	}

}
