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

@Entity(name="MS_FPS_COLUMN_RELATION")
@Table(name="MS_FPS_COLUMN_RELATION")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnRelationEntity {
	
	@Id
	@Column(name="ID")
	@GeneratedValue(generator = "SEQ_MS_FPS_COLUMN_RELATION", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEQ_MS_FPS_COLUMN_RELATION", sequenceName = "SEQ_MS_FPS_COLUMN_RELATION",allocationSize = 1)
	private Long id;
	
	@Column(name="BU")
	private String bu;
		
	@Column(name="PLATFORM")
	private String platform;
	
	@Column(name="DATA_CATEGORY")
	private String dataCategory;
		
	@Column(name="SOURCE_KEY")
	private String sourceKey;
	
	@Column(name="COLUMN_NAME")
	private String columnName;	
	
	@Column(name="COLUMN_POSITION")
	private Long position;
	
	@Column(name="COLUMN_ALIAS_NAME")
	private String aliasName;
	
	@Column(name="COLUMN_UUID")
	//@GeneratedValue
	//@Type(type = "org.hibernate.type.UUIDCharType")
	private String uuid;
	
	@Column(name="REQUIRED")
	private String required;
	
	@Column(name="COLUMN_REQUIRED_ERROR_FILE")
	private String columnRequiredInErrorFile;
	
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

	@Column(name="STATUS")
	private String status;
	
	@Column(name="X_PATH")
	private String path;
}
