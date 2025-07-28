package com.adp.esi.digitech.ds.config.repo;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.adp.esi.digitech.ds.config.entity.TransformationRulesEntity;

import jakarta.persistence.QueryHint;


@Repository
public interface TransformationRulesRepository extends JpaRepository<TransformationRulesEntity,Long> {
	
	@Query(nativeQuery = true ,value = "SELECT * FROM MS_FPS_TRANSFORMATION_RULES WHERE (STATUS != 'DELETED' OR STATUS IS NULL)")
	@QueryHints(value = { @QueryHint(name = "org.hibernate.fetchSize", value = "1000"),
						  @QueryHint(name = "org.hibernate.cacheable", value = "false") 
	})
	public Stream<TransformationRulesEntity> findAllTransformationRules();
	
	//public List<TransformationRulesEntity> findByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);
	
	@Query(nativeQuery = true ,value = "SELECT T.ID, T.BU, T.PLATFORM, T.DATA_CATEGORY, T.SOURCE_COLUMN_NAME, T.TARGET_COLUMN_NAME,"
			+ "T.TARGET_FILE_NAME, T.COLUMN_SEQUENCE, T.USEREMAIL, T.USERROLE, T.DEFAULT_VALUE, T.SUB_DATA_CATEGORY, T.DATA_TRANSFORMATION_RULES, T.REMOVE_SPECIAL_CHAR, "
			+ "T.TRANSFORMATION_REQUIRED, V.DATA_TYPE, V.LOV_CHECK_TYPE, V.TRANSFORMATION_REQUIRED, V.FORMAT, V.LOV_VALIDATION_REQUIRED, V.DEPENDS_ON FROM MS_FPS_TRANSFORMATION_RULES T LEFT OUTER JOIN MS_FPS_VALIDATION_RULES V "
			+ "ON T.BU = V.BU AND T.PLATFORM = V.PLATFORM AND T.DATA_CATEGORY = V.DATA_CATEGORY AND T.SOURCE_COLUMN_NAME = V.SOURCE_COLUMN AND V.VALIDATION_TYPE = 'client'"
			+ " WHERE T.BU=:bu AND T.PLATFORM=:platform AND T.DATA_CATEGORY=:dataCategory AND (T.STATUS != 'DELETED' OR T.STATUS IS NULL) order by T.ID")
	public List<Object[]> findByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);
	
	@Query(nativeQuery = true ,value = "SELECT TFR.id FROM MS_FPS_TRANSFORMATION_RULES TFR WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:dataCategory AND (STATUS != 'DELETED' OR STATUS IS NULL)")
	public List<Long> findIdByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);
	
	/*
	@Query(nativeQuery = true ,value = "SELECT TFR.* FROM MS_FPS_TRANSFORMATION_RULES TFR WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:dataCategory order by column_sequence")
	public List<TransformationRulesEntity> findByBuPlatformAndDataCategory(
			@Param("bu") String bu 
			,@Param("platform") String platform
			,@Param("dataCategory") String dataCategory);
	
	
	
	@Query(nativeQuery = true ,value = "SELECT TFR.* FROM MS_FPS_TRANSFORMATION_RULES TFR WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:dataCategory AND SUB_DATA_CATEGORY=:subDataCategory order by column_sequence")
	public List<TransformationRulesEntity> findByBuPlatformDataCategoryAndSubDataCategory(
			@Param("bu") String bu 
			,@Param("platform") String platform
			,@Param("dataCategory") String dataCategory
			,@Param("subDataCategory") String subDataCategory);
	*/
	public List<TransformationRulesEntity> findBySourceColumnNameIn(List<String> uuid);
	
	public void deleteByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);
	
	public long countByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);
	
	@Modifying
	@Query(nativeQuery = true ,value = "UPDATE MS_FPS_TRANSFORMATION_RULES SET STATUS = 'DELETED', USEREMAIL=:useremail, USERROLE=:userrole, MODIFIED_DATE_TIME = CURRENT_TIMESTAMP WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:dataCategory")
	public void softDeleteByBuAndPlatformAndDataCategory(@Param("bu") String bu, @Param("platform") String platform, @Param("dataCategory") String dataCategory,  @Param("useremail") String useremail, @Param("userrole") String  userrole);
	
	@Modifying
	@Query(nativeQuery = true ,value = "UPDATE MS_FPS_TRANSFORMATION_RULES SET STATUS = 'DELETED', USEREMAIL=:useremail, USERROLE=:userrole, MODIFIED_DATE_TIME = CURRENT_TIMESTAMP WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:dataCategory AND SOURCE_COLUMN_NAME in (:uuids)")
	public void softDeletefield(@Param("bu") String bu, @Param("platform") String platform, @Param("dataCategory") String dataCategory, @Param("uuids") List<String> uuids, @Param("useremail") String useremail, @Param("userrole") String  userrole);
	
	@Modifying
	@Query(nativeQuery = true ,value = "UPDATE MS_FPS_TRANSFORMATION_RULES SET DATA_CATEGORY = :newDataCategory, USEREMAIL=:useremail, USERROLE=:userrole, MODIFIED_DATE_TIME = CURRENT_TIMESTAMP WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:oldDataCategory")
	public void updateDataCategory(@Param("bu") String bu, @Param("platform") String platform, @Param("oldDataCategory") String oldDataCategory, @Param("newDataCategory") String newDataCategory, @Param("useremail") String useremail, @Param("userrole") String  userrole);
}
