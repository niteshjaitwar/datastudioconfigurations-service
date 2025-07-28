package com.adp.esi.digitech.ds.config.repo;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.adp.esi.digitech.ds.config.entity.ValidationsRulesEntity;

import jakarta.persistence.QueryHint;

@Repository
public interface ValidationRulesRepository extends JpaRepository<ValidationsRulesEntity, Long>{
	
	@Query(nativeQuery = true ,value = "SELECT * FROM MS_FPS_VALIDATION_RULES WHERE (STATUS != 'DELETED' OR STATUS IS NULL)")
	@QueryHints(value = { @QueryHint(name = "org.hibernate.fetchSize", value = "1000"),
						  @QueryHint(name = "org.hibernate.cacheable", value = "false") 
	})
	public Stream<ValidationsRulesEntity> findAllValidationRules();
	
	public List<ValidationsRulesEntity> findByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);
	
	@Query(nativeQuery = true ,value = "SELECT v.id FROM MS_FPS_VALIDATION_RULES v WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:dataCategory AND (STATUS != 'DELETED' OR STATUS IS NULL)")
	public List<Long> findIdByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);
	
	@Query(nativeQuery = true ,value = "SELECT v.* FROM MS_FPS_VALIDATION_RULES v WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:dataCategory AND VALIDATION_TYPE=:validationType AND (STATUS != 'DELETED' OR STATUS IS NULL)")
	public List<ValidationsRulesEntity> findByBuPlatformAndDataCategory(
			@Param("bu") String bu 
			,@Param("platform") String platform
			,@Param("dataCategory") String dataCategory
			,@Param("validationType") String validationType);
	
	
	
	@Query(nativeQuery = true ,value = "SELECT v.* FROM MS_FPS_VALIDATION_RULES v WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:dataCategory AND VALIDATION_TYPE=:validationType AND SUB_DATA_CATEGORY=:subDataCategory AND (STATUS != 'DELETED' OR STATUS IS NULL)")
	public List<ValidationsRulesEntity> findByBuPlatformDataCategoryAndSubDataCategory(
			@Param("bu") String bu 
			,@Param("platform") String platform
			,@Param("dataCategory") String dataCategory
			,@Param("validationType") String validationType
			,@Param("subDataCategory") String subDataCategory);
	
	
	public List<ValidationsRulesEntity> findByValidationRuleTypeAndSourceColumnIn(String validationRuleType,List<String> sourceColumnUuids);
	
	public void deleteByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);
	
	public long countByBuAndPlatformAndDataCategory(String bu, String platform, String dataCategory);
	
	/*@Query("select new com.adp.digitech.fileprocessing.model.RulesDataResponse(T.bu,T.platform,T.dataCategory,T.sourceColumnName,V.isMandetory,V.dataTransformationRules) from MS_FPS_VALIDATION_RULES V, MS_FPS_TRANSFORMATION_RULES T WHERE V.bu=T.bu AND V.platform=T.platform AND V.dataCategory=T.dataCategory AND V.sourceColumn=T.sourceColumnName  and  V.bu != 'TEST' AND T.bu!='TEST' AND V.platform='ADP DE'")
	public List<RulesDataResponse> getValidationTransformationRules();*/
	
	@Modifying
	@Query(nativeQuery = true ,value = "UPDATE MS_FPS_VALIDATION_RULES SET STATUS = 'DELETED', USEREMAIL=:useremail, USERROLE=:userrole, MODIFIED_DATE_TIME = CURRENT_TIMESTAMP WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:dataCategory")
	public void softDeleteByBuAndPlatformAndDataCategory(@Param("bu") String bu, @Param("platform") String platform, @Param("dataCategory") String dataCategory,  @Param("useremail") String useremail, @Param("userrole") String  userrole);
	
	@Modifying
	@Query(nativeQuery = true ,value = "UPDATE MS_FPS_VALIDATION_RULES SET STATUS = 'DELETED', USEREMAIL=:useremail, USERROLE=:userrole, MODIFIED_DATE_TIME = CURRENT_TIMESTAMP WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:dataCategory AND SOURCE_COLUMN IN (:uuids)")
	public void softDeletefield(@Param("bu") String bu, @Param("platform") String platform, @Param("dataCategory") String dataCategory,  @Param("uuids") List<String> uuids, @Param("useremail") String useremail, @Param("userrole") String  userrole);

	@Modifying
	@Query(nativeQuery = true ,value = "UPDATE MS_FPS_VALIDATION_RULES SET DATA_CATEGORY =:newDataCategory, USEREMAIL=:useremail, USERROLE=:userrole, MODIFIED_DATE_TIME = CURRENT_TIMESTAMP WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:oldDataCategory")
	public void updateDataCategory(@Param("bu") String bu, @Param("platform") String platform, @Param("oldDataCategory") String oldDataCategory, @Param("newDataCategory") String newDataCategory, @Param("useremail") String useremail, @Param("userrole") String  userrole);
}
 