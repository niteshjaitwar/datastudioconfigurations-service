package com.adp.esi.digitech.ds.config.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Pagination {
	
	private int count;
	private int pages;
	private String type;
	private String schema;
	
}
