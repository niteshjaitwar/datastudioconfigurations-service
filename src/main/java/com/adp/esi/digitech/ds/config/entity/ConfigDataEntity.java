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
@Entity(name = "MS_FPS_CONFIGURATION_DATA")
@Table(name = "MS_FPS_CONFIGURATION_DATA")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ConfigDataEntity {

	
	@Id
	@GeneratedValue(generator = "SEQ_MS_FPS_CONFIGURATION_DATA", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEQ_MS_FPS_CONFIGURATION_DATA", sequenceName = "SEQ_MS_FPS_CONFIGURATION_DATA",allocationSize = 1)
	
	@Column(name="ID")
	private Long id;
		
	@Column(name="BU")
	private String bu;
		
	@Column(name="PLATFORM")
	private String platform;
	
	@Column(name="DATA_CATEGORY")
	private String dataCategory;
	
	@Column(name="SUB_DATA_CATEGORY")
	private String subDataCategory;	
	
	@Column(name="OUTPUT_FILE_RULES", columnDefinition = "CLOB")
	@Lob
	private String outputFileRules;
	
	@Column(name="APP_CODE")
	private String appCode;	
	
	@Column(name="SOURCE")
	private String source;	
	
	@Column(name="INPUT_RULES", columnDefinition = "CLOB")
	@Lob
	private String inputRules;
	
	@Column(name="USEREMAIL")
	private String useremail;
	
	@Column(name="USERROLE")
	private String userrole;
	
	@Column(name="MODIFIED_DATE_TIME")
	private Timestamp modifiedDateTime;
	
	@Column(name="FILES_INFO", columnDefinition = "CLOB")
	@Lob
	private String filesInfo;
	
	@Column(name="DATA_RULES", columnDefinition = "CLOB")
	@Lob
	private String dataRules;
	
	@Column(name="TARGET_LOCATION")
	private String targetLocation;	
	
	@Column(name="TARGET_PATH")
	private String targetPath;
	
	@Column(name="PROCESS_TYPE")
	private String processType;	
	
	@Column(name="PROCESS_STEPS")
	private String processSteps;
}