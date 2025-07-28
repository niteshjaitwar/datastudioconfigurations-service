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


@Entity(name="MS_FPS_VALIDATION_RULES")
@Table(name="MS_FPS_VALIDATION_RULES")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class ValidationsRulesEntity {
	
	
	@Id
	@Column(name="ID")
	@GeneratedValue(generator = "SEQ_MS_FPS_VALIDATION_RULES", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEQ_MS_FPS_VALIDATION_RULES", sequenceName = "SEQ_MS_FPS_VALIDATION_RULES",allocationSize = 1)
	private Long id;
		
	@Column(name="BU")
	private String bu;
	
	@Column(name="PLATFORM")
	private String platform;
	
	@Column(name="DATA_CATEGORY")
	private String dataCategory;	
	
	@Column(name="SUB_DATA_CATEGORY")
	private String subDataCategory;	
	
	@Column(name="SOURCE_COLUMN")
	private String sourceColumn;
	
	@Column(name="DATA_TYPE")
	private String dataType;
	
	@Column(name="MANDATORY")
	private String isMandatory;
	
	@Column(name="MAX_LENGTH")
	private String maxLengthAllowed;
	
	@Column(name="FORMAT")
	private String dataFormat;
	
	@Column(name="MIN_VALUE")
	private String minValue;
	
	@Column(name="MAX_VALUE")
	private String maxValue;
	
	@Column(name="SPECIAL_CHAR_NOT_ALLOWED")
	private String specialCharNotAllowed;
	
	@Column(name="LOV_CHECK_TYPE")
	private String lovCheckType;
	
	@Column(name="TRANSFORMATION_REQUIRED")
	private String transformationRequired;
	
	@Column(name="COLUMN_REQUIRED_ERROR_FILE")
	private String columnRequiredInErrorFile;
	
	@Column(name="UNIQUE_COLUMN_VALUE")
	private String uniqueValueInColumn;
	
	@Column(name="CONDATIONAL_VALIDATION_RULES")
	private String conditionalValidationRule;
	
	@Column(name="DATA_TRANSFORMATION_RULES")
	private String dataTransformationRules;
	
	@Column(name="REMOVE_SPECIAL_CHAR")
	private String specialCharToBeRemoved;
	
	@Column(name="DATA_EXCLUSION_RULES")
	private String dataExclusionRules;
	
	@Column(name="VALIDATION_TYPE")
	private String validationRuleType;
	
	@Column(name="MIN_LENGTH")
	private String minLengthAllowed;
	
	@Column(name="STRING_CHECK_RULES")
	private String stringCheckRule;
	
	/*@Column(name="REQUIRED_CONCAT")
	private String concatRequired;*/
	
	@Column(name="USEREMAIL")
	private String useremail;
	
	@Column(name="USERROLE")
	private String userrole;
	
	@Column(name="MODIFIED_DATE_TIME")
	private Timestamp modifiedDateTime;
	
	@Column(name="LOV_VALIDATION_REQUIRED")
	private String lovValidationRequired;
	
	@Column(name="STATUS")
	private String status;
	
	@Column(name="DEPENDS_ON")
	private String dependsOn;
	
	/*@OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
	@JoinColumns({
		@JoinColumn(name="rl_bu",referencedColumnName = "BU"),
		@JoinColumn(name="rl_platform",referencedColumnName = "PLATFORM"),
		@JoinColumn(name="rl_category",referencedColumnName = "DATA_CATEGORY"),
		@JoinColumn(name="rl_source",referencedColumnName = "SOURCE_COLUMN")
		})
	
	private List<TransformationRulesEntity> transformrules;*/
}
