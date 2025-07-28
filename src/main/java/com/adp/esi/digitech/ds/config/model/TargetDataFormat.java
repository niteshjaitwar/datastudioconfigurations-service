package com.adp.esi.digitech.ds.config.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetDataFormat {
	
	private String targetType;
	private String targetFormat;
	private String targetDecimalAllowed;
}
