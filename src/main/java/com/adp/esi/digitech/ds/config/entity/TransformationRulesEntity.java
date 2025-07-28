package com.adp.esi.digitech.ds.config.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;



@Entity(name="MS_FPS_TRANSFORMATION_RULES")
@Table(name="MS_FPS_TRANSFORMATION_RULES")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TransformationRulesEntity {

	
	@Id
	@GeneratedValue(generator = "SEQ_MS_FPS_TRANSFORMATION_RULES", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEQ_MS_FPS_TRANSFORMATION_RULES", sequenceName = "SEQ_MS_FPS_TRANSFORMATION_RULES",allocationSize = 1)
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
		
	@Column(name="SOURCE_COLUMN_NAME")
	private String sourceColumnName;
	
	@Column(name="TARGET_COLUMN_NAME")
	private String targetColumnName;
	
	@Column(name="TARGET_FILE_NAME")
	private String targetFileName;	
	
	@Column (name="COLUMN_SEQUENCE")
	private Integer columnSequence;
	
	@Column (name="DEFAULT_VALUE")
	private String defaultValue;
	
	@Column(name="DATA_TRANSFORMATION_RULES")
	private String dataTransformationRules;
	
	@Column(name="REMOVE_SPECIAL_CHAR")
	private String specialCharToBeRemoved;
	
	@Column(name="TRANSFORMATION_REQUIRED")
	private String transformationRequired;
	
	//@Column (name="JOIN_OPERATOR")
	//private String joinBy;
	
	@Column(name="USEREMAIL")
	private String useremail;
	
	@Column(name="USERROLE")
	private String userrole;
	
	@Column(name="MODIFIED_DATE_TIME")
	private Timestamp modifiedDateTime;
	
	@Column(name="STATUS")
	private String status;
}
