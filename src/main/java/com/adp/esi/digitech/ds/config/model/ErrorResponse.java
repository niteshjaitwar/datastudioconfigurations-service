package com.adp.esi.digitech.ds.config.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

public final class ErrorResponse {
	
	public final String code;
	public final String message;
	public List<ErrorData> errors;
	
	public ErrorResponse(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	
	@JsonCreator
	public ErrorResponse(String code, String message, List<ErrorData> errors) {
		this.code = code;
		this.message = message;
		this.errors = errors;
	}

}
