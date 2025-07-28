package com.adp.esi.digitech.ds.config.repo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.adp.esi.digitech.ds.config.entity.ConfigDataEntity;

import jakarta.persistence.QueryHint;

@Repository
public interface ConfigDataRepository extends JpaRepository<ConfigDataEntity, Long>, JpaSpecificationExecutor<ConfigDataEntity> {
	
	@Query(nativeQuery = true ,value = "SELECT * FROM MS_FPS_CONFIGURATION_DATA")
	@QueryHints(value = { @QueryHint(name = "org.hibernate.fetchSize", value = "500"),
						  @QueryHint(name = "org.hibernate.cacheable", value = "false") 
	})
	public Stream<ConfigDataEntity> findAllConfigData();

	@Query(nativeQuery = true ,value = "select c.* from MS_FPS_CONFIGURATION_DATA c where BU=:bu and PLATFORM=:platform")
	public List<ConfigDataEntity> findByBUAndPlatform(@Param("bu") String bu, @Param("platform") String platform);

	@Query(nativeQuery = true ,value = "select c.* from MS_FPS_CONFIGURATION_DATA c where BU=:bu")
	public List<ConfigDataEntity> findByBU(@Param("bu") String bu);
	
	@Query(nativeQuery = true, value = "select distinct trim(BU) BU, trim(PLATFORM) PLATFORM, trim(DATA_CATEGORY) DATA_CATEGORY from MS_FPS_CONFIGURATION_DATA where BU is not null and PLATFORM is not null and DATA_CATEGORY is not null order by BU, PLATFORM, DATA_CATEGORY asc")
	public List<Object[]> findBUPlatformAndDataCategoryBasicInfo();
	
	@Query(nativeQuery = true, value = "select distinct trim(BU) BU, trim(PLATFORM) PLATFORM, trim(DATA_CATEGORY) DATA_CATEGORY from MS_FPS_CONFIGURATION_DATA where BU is not null and PLATFORM is not null and DATA_CATEGORY is not null and (SOURCE IS NULL OR SOURCE NOT IN(:sources)) order by BU, PLATFORM, DATA_CATEGORY asc")
	public List<Object[]> findBUPlatformAndDataCategoryAndSourceBasicInfo(List<String> sources);

	@Query(nativeQuery = true ,value = "select c.* from MS_FPS_CONFIGURATION_DATA c where BU=:bu and PLATFORM=:platform and DATA_CATEGORY=:dataCategory")
	public Optional<ConfigDataEntity> findByBUPlatformAndDataCategory(@Param("bu") String bu,
			@Param("platform") String platform, @Param("dataCategory") String dataCategory);

	@Query(nativeQuery = true ,value = "select c.* from MS_FPS_CONFIGURATION_DATA c where BU=:bu and PLATFORM=:platform and DATA_CATEGORY=:dataCategory and SUB_DATA_CATEGORY=:subDataCategory")
	public Optional<ConfigDataEntity> findByBUPlatformDataCategoryAndSubDataCategory(@Param("bu") String bu,
			@Param("platform") String platform, @Param("dataCategory") String dataCategory, @Param("subDataCategory") String subDataCategory);
	
	public void deleteByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);

	public long countByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);
	
	@Modifying
	@Query(nativeQuery = true ,value = "UPDATE MS_FPS_CONFIGURATION_DATA SET SOURCE = 'DELETED', USEREMAIL=:useremail, USERROLE=:userrole, MODIFIED_DATE_TIME = CURRENT_TIMESTAMP WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:dataCategory")
	public void softDeleteByBuAndPlatformAndDataCategory(@Param("bu") String bu, @Param("platform") String platform, @Param("dataCategory") String dataCategory, @Param("useremail") String useremail, @Param("userrole") String  userrole);
	
	@Modifying
	@Query(nativeQuery = true ,value = "UPDATE MS_FPS_CONFIGURATION_DATA SET DATA_CATEGORY =:newDataCategory, FILES_INFO =:fileInfo, INPUT_RULES=:inputRules, USEREMAIL=:useremail, USERROLE=:userrole, MODIFIED_DATE_TIME = CURRENT_TIMESTAMP WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:oldDataCategory")
	public void updateDataCategory(@Param("bu") String bu, @Param("platform") String platform, @Param("oldDataCategory") String oldDataCategory, @Param("newDataCategory") String newDataCategory, @Param("fileInfo") String fileInfo, @Param("inputRules") String inputRules, @Param("useremail") String useremail, @Param("userrole") String  userrole);
	
}
