package com.adp.esi.digitech.ds.config.service;

import java.text.SimpleDateFormat;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;

public abstract class AbstractConfigService<T> implements IConfigService<T>{
	

	public ModelMapper modelMapper;	

	public SimpleDateFormat simpleDateFormat;
	
	public EntityManager entityManager;
	
	public ObjectMapper objectMapper;
	

	@Autowired
	public void setModelMapper(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	@Autowired
	public void setSimpleDateFormat(SimpleDateFormat simpleDateFormat) {
		this.simpleDateFormat = simpleDateFormat;
	}

	@Autowired
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Autowired
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	

}
