package com.adp.esi.digitech.ds.config.exception;

import java.util.List;

import com.adp.esi.digitech.ds.config.model.ErrorData;

public class ConfigurationException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6738289860296647495L;
	private List<ErrorData> errors;
	
	

	public ConfigurationException(String message) {
		super(message);
		
	}
	
	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public void setErrors(List<ErrorData> errors) {
		this.errors = errors;
	}

	public List<ErrorData> getErrors() {
		return errors;
	}
}
