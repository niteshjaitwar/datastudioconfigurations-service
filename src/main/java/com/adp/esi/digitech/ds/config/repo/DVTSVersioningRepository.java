package com.adp.esi.digitech.ds.config.repo;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import com.adp.esi.digitech.ds.config.entity.DVTSVersioningEntity;

import jakarta.persistence.QueryHint;

public interface DVTSVersioningRepository extends JpaRepository<DVTSVersioningEntity, Long> {
	
	@Query(nativeQuery = true ,value = "SELECT * FROM MS_FPS_DVTS_VERSIONING")
	@QueryHints(value = { @QueryHint(name = "org.hibernate.fetchSize", value = "1000"),
						  @QueryHint(name = "org.hibernate.cacheable", value = "false") 
	})
	public Stream<DVTSVersioningEntity> findAllVersions();
	
	public List<DVTSVersioningEntity> findByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);
	
	public void deleteByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);
	
}
