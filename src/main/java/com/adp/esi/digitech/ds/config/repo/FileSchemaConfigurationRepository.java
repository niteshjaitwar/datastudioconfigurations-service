package com.adp.esi.digitech.ds.config.repo;

import com.adp.esi.digitech.ds.config.entity.FileSchemaConfigurationEntity;

import jakarta.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Repository for MS FPS file schema configuration entities. Provides data
 * access operations for IBPMADMIN.MS_FPS_FILE_SCHEMA_CONFIGURATION table.
 * 
 * @author rhidau
 */
@Repository
public interface FileSchemaConfigurationRepository
		extends JpaRepository<FileSchemaConfigurationEntity, Long> , JpaSpecificationExecutor<FileSchemaConfigurationEntity> {
	
	
	@Query(nativeQuery = true ,value = "SELECT * FROM MS_FPS_FILE_SCHEMA_CONFIGURATION")
	@QueryHints(value = { @QueryHint(name = "org.hibernate.fetchSize", value = "500"),
						  @QueryHint(name = "org.hibernate.cacheable", value = "false") 
	})
	Stream<FileSchemaConfigurationEntity> findAllFileSchemaConfigurations();

	/**
	 * Finds schema configuration by source key.
	 *
	 * @param sourceKey the unique source key
	 * @return optional schema configuration entity
	 */
	Optional<FileSchemaConfigurationEntity> findBySourceKey(String sourceKey);

	/**
	 * Finds schema configurations by business unit, platform, and data category.
	 *
	 * @param bu           the business unit
	 * @param platform     the platform name
	 * @param dataCategory the data category
	 * @return list of matching schema configuration entities
	 */
	List<FileSchemaConfigurationEntity> findByBuAndPlatformAndDataCategory(String bu, String platform,
			String dataCategory);

	/**
	 * Finds schema configuration by all four key parameters.
	 *
	 * @param bu           the business unit
	 * @param platform     the platform name
	 * @param dataCategory the data category
	 * @param sourceKey    the source key
	 * @return optional schema configuration entity
	 */
	Optional<FileSchemaConfigurationEntity> findByBuAndPlatformAndDataCategoryAndSourceKeyAndVersion(String bu,
			String platform, String dataCategory, String sourceKey, String version);

	/**
	 * Checks if schema configuration exists by source key.
	 *
	 * @param sourceKey the unique source key
	 * @return true if exists, false otherwise
	 */
	boolean existsBySourceKey(String sourceKey);

	/**
	 * Checks if schema configuration exists by all four key parameters.
	 *
	 * @param bu           the business unit
	 * @param platform     the platform name
	 * @param dataCategory the data category
	 * @param sourceKey    the source key
	 * @return true if exists, false otherwise
	 */
	boolean existsByBuAndPlatformAndDataCategoryAndSourceKey(String bu, String platform, String dataCategory,
			String sourceKey);

	/**
	 * Finds the latest version for a given source key using native query to avoid
	 * case sensitivity issues.
	 *
	 * @param sourceKey the source key
	 * @return the latest version string
	 */
	@Query(value = "SELECT MAX(VERSION) FROM IBPMADMIN.MS_FPS_FILE_SCHEMA_CONFIGURATION WHERE SOURCE_KEY = :sourceKey", nativeQuery = true)
	String findLatestVersionBySourceKey(@Param("sourceKey") String sourceKey);

	/**
	 * Counts schema configurations by business unit, platform, and data category.
	 *
	 * @param bu           the business unit
	 * @param platform     the platform name
	 * @param dataCategory the data category
	 * @return count of matching configurations
	 */
	long countByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);

	/**
	 * Deletes schema configurations by business unit, platform, and data category.
	 *
	 * @param bu           the business unit
	 * @param platform     the platform name
	 * @param dataCategory the data category
	 */
	void deleteByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);

	/**
	 * Deletes schema configuration by all four key parameters.
	 *
	 * @param bu           the business unit
	 * @param platform     the platform name
	 * @param dataCategory the data category
	 * @param sourceKey    the source key
	 */
	void deleteByBuAndPlatformAndDataCategoryAndSourceKey(String bu, String platform, String dataCategory,
			String sourceKey);
}
