package com.adp.esi.digitech.ds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AhubDatastudioconfigurationsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AhubDatastudioconfigurationsServiceApplication.class, args);
	}

}
