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
import lombok.ToString;

@Entity(name="MS_FPS_FILE_POLLING_CONFIGURATION")
@Table(name="MS_FPS_FILE_POLLING_CONFIGURATION")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class FilePollingConfigurationEntity {
	
	@Id
	@Column(name="ID")
	@GeneratedValue(generator = "SEQ_MS_FPS_FILE_POLLING_CONFIGURATION", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEQ_MS_FPS_FILE_POLLING_CONFIGURATION", sequenceName = "SEQ_MS_FPS_FILE_POLLING_CONFIGURATION",allocationSize = 1)
	private Long id;
	
	@Column(name="BU")
	private String bu;
	
	@Column(name="LOCATION")
	private String location;
	
	@Column(name="APP_CODE")
	private String appCode;
	
	@Column(name="SCHEDULAR")
	private String schedular;
	
	@Column(name="CONFIG", columnDefinition = "CLOB")
	@Lob
	private String config;
	
	@Column(name="CREATED_BY")
	private String createdBy;
	
	@Column(name="CREATED_DATE_TIME")
	private Timestamp createdDateTime;	
	
	@Column(name="MODIFIED_BY")
	private String modifiedBy;
	
	@Column(name="MODIFIED_DATE_TIME")
	private Timestamp modifiedDateTime;
	
	@Column(name="USERROLE")
	private String userrole;

}
