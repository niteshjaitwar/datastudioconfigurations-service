package com.adp.esi.digitech.ds.config.entity;

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

@Entity(name="MS_FPS_LOV_METADATA")
@Table(name="MS_FPS_LOV_METADATA")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LOVTypeEntity {
	
	@Id
	@Column(name="ID")
	@GeneratedValue(generator = "SEQ_MS_FPS_LOV_METADATA", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEQ_MS_FPS_LOV_METADATA", sequenceName = "SEQ_MS_FPS_LOV_METADATA",allocationSize = 50)
	private Long id;
		
	@Column(name="LOV_TYPE")
	private String lovType;
	
	@Column(name="LOV_NAME")
	private String lovName;
	
	@Column(name="LOV_VALUE")
	private String lovValue;

}
