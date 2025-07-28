package com.adp.esi.digitech.ds.config.model;

import lombok.Data;

@Data
public class ComplexLovData {
	private Long id;
	private String lovType;
	private String lovDataJson;
	private int lovRecordsCount;
	private String lovSchema;
}
