package com.adp.esi.digitech.ds.config.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity(name = "MS_FPS_COMPLEX_LOV_METADATA")
@Table(name = "MS_FPS_COMPLEX_LOV_METADATA")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ComplexLOVTypeEntity {

	@Id
	@Column(name = "ID")
	@GeneratedValue(generator = "SEQ_MS_FPS_COMPLEX_LOV_METADATA", strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "SEQ_MS_FPS_COMPLEX_LOV_METADATA", sequenceName = "SEQ_MS_FPS_COMPLEX_LOV_METADATA",allocationSize = 1)
	private Long id;
	
	@Column(name="LOV_TYPE")
	private String lovType;
	
	@Column(name= "LOV_DATA_JSON", columnDefinition = "CLOB")
	@Lob
	private String lovDataJson;
	
	@Column(name = "LOV_RECORDS_COUNT")
	private int lovRecordsCount;
	
	@Column(name= "LOV_SCHEMA")
	private String lovSchema;
}
