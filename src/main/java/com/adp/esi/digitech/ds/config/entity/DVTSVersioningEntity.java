package com.adp.esi.digitech.ds.config.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name="MS_FPS_DVTS_VERSIONING")
@Table(name="MS_FPS_DVTS_VERSIONING")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DVTSVersioningEntity {
	
	@Id
	@Column(name="ID")
	@GeneratedValue(generator = "SEQ_MS_FPS_DVTS_VERSIONING", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEQ_MS_FPS_DVTS_VERSIONING", sequenceName = "SEQ_MS_FPS_DVTS_VERSIONING",allocationSize = 1)
	private Long id;
	
	@Column(name="BU")
	private String bu;
		
	@Column(name="PLATFORM")
	private String platform;
	
	@Column(name="DATA_CATEGORY")
	private String dataCategory;
	
	@Column(name="VERSION_NAME")
	private String versionName;
	
	@Column(name="DESCRIPTION")
	private String description;
	
	@Column(name="DVTS_CONFIG_DATA", columnDefinition = "CLOB")
	@Lob
	private String dvtsConfigData;
	
	@Column(name="USERROLE")
	private String userrole;
	
	@Column(name="CREATED_BY")
	private String createdBy;
	
	@Column(name="CREATED_DATE_TIME")
	private Timestamp createdDateTime;	
	
	@Column(name="MODIFIED_BY")
	private String modifiedBy;
	
	@Column(name="MODIFIED_DATE_TIME")
	private Timestamp modifiedDateTime;	
	
}
