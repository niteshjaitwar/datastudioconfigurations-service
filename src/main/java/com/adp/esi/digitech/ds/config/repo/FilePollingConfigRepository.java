package com.adp.esi.digitech.ds.config.repo;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import com.adp.esi.digitech.ds.config.entity.FilePollingConfigurationEntity;

import jakarta.persistence.QueryHint;

public interface FilePollingConfigRepository extends JpaRepository<FilePollingConfigurationEntity, Long> {
	
	
	@Query(nativeQuery = true ,value = "SELECT * FROM MS_FPS_FILE_POLLING_CONFIGURATION")
	@QueryHints(value = { @QueryHint(name = "org.hibernate.fetchSize", value = "1000"),
						  @QueryHint(name = "org.hibernate.cacheable", value = "false") 
	})
	public Stream<FilePollingConfigurationEntity> findAllFilePollingConfigs();

	public FilePollingConfigurationEntity findByBu(String bu);
	
}
