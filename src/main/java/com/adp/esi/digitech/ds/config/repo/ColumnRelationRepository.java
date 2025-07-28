package com.adp.esi.digitech.ds.config.repo;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.adp.esi.digitech.ds.config.entity.ColumnRelationEntity;

import jakarta.persistence.QueryHint;

public interface ColumnRelationRepository extends JpaRepository<ColumnRelationEntity, Long> {
	
	
	@Query(nativeQuery = true ,value = "SELECT * FROM MS_FPS_COLUMN_RELATION WHERE (STATUS != 'DELETED' OR STATUS IS NULL)")
	@QueryHints(value = { @QueryHint(name = "org.hibernate.fetchSize", value = "1000"),
						  @QueryHint(name = "org.hibernate.cacheable", value = "false") 
	})
	public Stream<ColumnRelationEntity> findAllColumnRelations();
	
	@Query(nativeQuery = true ,value = "SELECT CR.id FROM MS_FPS_COLUMN_RELATION CR WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:dataCategory AND (STATUS != 'DELETED' OR STATUS IS NULL)")
	public List<Long> findIdByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);
	
	@Query(nativeQuery = true ,value = "SELECT C.ID, C.BU, C.PLATFORM, C.DATA_CATEGORY, C.SOURCE_KEY, C.COLUMN_NAME,"
			+ "C.COLUMN_POSITION, C.COLUMN_ALIAS_NAME, C.COLUMN_UUID, C.REQUIRED, C.COLUMN_REQUIRED_ERROR_FILE,"
			+ "CASE WHEN c.modified_by IS NOT NULL THEN c.modified_by ELSE c.created_by END AS useremail,C.USERROLE,  "
			+ "V.DATA_EXCLUSION_RULES, V.DATA_TYPE, V.FORMAT, C.X_PATH  FROM MS_FPS_COLUMN_RELATION C LEFT OUTER JOIN MS_FPS_VALIDATION_RULES V "
			+ "ON C.BU = V.BU AND C.PLATFORM = V.PLATFORM AND C.DATA_CATEGORY = V.DATA_CATEGORY AND C.COLUMN_UUID = V.SOURCE_COLUMN"
			+ " WHERE C.BU=:bu AND C.PLATFORM=:platform AND C.DATA_CATEGORY=:dataCategory AND V.VALIDATION_TYPE='client' AND (C.STATUS != 'DELETED' OR C.STATUS IS NULL) order by C.ID")
	public	List<Object[]> findByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);
	
	//public	List<ColumnRelationEntity> findByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);

	public	List<ColumnRelationEntity> findByBuAndPlatformAndDataCategoryAndSourceKeyLike(String bu, String platform, String dataCategory,String sourceKey);

	public List<ColumnRelationEntity> findByUuidIn(List<String> uuid);
	
	public Long deleteByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);
	
	public Long countByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);
	
	@Modifying
	@Query(nativeQuery = true ,value = "UPDATE MS_FPS_COLUMN_RELATION SET STATUS = 'DELETED', MODIFIED_BY=:useremail, USERROLE=:userrole, MODIFIED_DATE_TIME = CURRENT_TIMESTAMP WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:dataCategory")
	public void softDeleteByBuAndPlatformAndDataCategory(@Param("bu") String bu, @Param("platform") String platform, @Param("dataCategory") String dataCategory, @Param("useremail") String useremail, @Param("userrole") String  userrole);
	
	@Modifying
	@Query(nativeQuery = true ,value = "UPDATE MS_FPS_COLUMN_RELATION SET STATUS = 'DELETED', MODIFIED_BY=:useremail, USERROLE=:userrole, MODIFIED_DATE_TIME = CURRENT_TIMESTAMP WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:dataCategory AND COLUMN_UUID in (:uuids)")
	public void softDeletefield(@Param("bu") String bu, @Param("platform") String platform, @Param("dataCategory") String dataCategory, @Param("uuids") List<String> uuids, @Param("useremail") String useremail, @Param("userrole") String  userrole);

	@Modifying
	@Query(nativeQuery = true ,value = "UPDATE MS_FPS_COLUMN_RELATION SET SOURCE_KEY = REPLACE(SOURCE_KEY, :oldDataCategory, :newDataCategory), MODIFIED_BY=:useremail, USERROLE=:userrole, MODIFIED_DATE_TIME = CURRENT_TIMESTAMP WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:oldDataCategory")
	public void updateSourceKey(@Param("bu") String bu, @Param("platform") String platform, @Param("oldDataCategory") String oldDataCategory, @Param("newDataCategory") String newDataCategory, @Param("useremail") String useremail, @Param("userrole") String  userrole);
	
	@Modifying
	@Query(nativeQuery = true ,value = "UPDATE MS_FPS_COLUMN_RELATION SET DATA_CATEGORY =:newDataCategory, MODIFIED_BY=:useremail, USERROLE=:userrole, MODIFIED_DATE_TIME = CURRENT_TIMESTAMP WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:oldDataCategory")
	public void updateDataCategory(@Param("bu") String bu, @Param("platform") String platform, @Param("oldDataCategory") String oldDataCategory, @Param("newDataCategory") String newDataCategory, @Param("useremail") String useremail, @Param("userrole") String  userrole);
}
