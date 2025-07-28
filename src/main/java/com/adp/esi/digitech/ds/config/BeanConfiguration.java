package com.adp.esi.digitech.ds.config;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class BeanConfiguration {
	
	@Value("${jasypt.encryptor.algorithm}")
	private String algorithm;
	
	@Value("${jasypt.encryptor.password}")
	private String password;
	
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
	
	@Bean
	public SimpleDateFormat simpleDateFormat() {
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		s.setTimeZone(TimeZone.getTimeZone("GMT"));
		return s;
	}
	
	@Bean
	public DataFormatter dataFormatter() {
		return new DataFormatter();
	}
	
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
	
	@Bean
	public StandardPBEStringEncryptor standardPBEStringEncryptor() {
		StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
		standardPBEStringEncryptor.setAlgorithm(algorithm);
		standardPBEStringEncryptor.setPassword(password);
		return standardPBEStringEncryptor;
	}
}
