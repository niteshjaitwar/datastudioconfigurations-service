package com.adp.esi.digitech.ds.config.repo;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.adp.esi.digitech.ds.config.entity.LOVTypeEntity;

import jakarta.persistence.QueryHint;

@Repository
public interface LOVTypeRepository extends JpaRepository<LOVTypeEntity, Long> , JpaSpecificationExecutor<LOVTypeEntity>{
	
	//public List<LOVTypeEntity> findByLovType(String lovType);
	
	@Query(nativeQuery = true ,value = "SELECT * FROM MS_FPS_LOV_METADATA")
	@QueryHints(value = { @QueryHint(name = "org.hibernate.fetchSize", value = "4000"),
						  @QueryHint(name = "org.hibernate.cacheable", value = "false") 
	})
	public Stream<LOVTypeEntity> findAllLov();
	
	@QueryHints(value = {
			@QueryHint(name = "org.hibernate.fetchSize", value = "4000"),
			@QueryHint(name = "org.hibernate.cacheable", value = "false")
	})
	public Stream<LOVTypeEntity> findByLovType(String lovType);
		
	public List<LOVTypeEntity> findByLovTypeIn(List<String>  lovTypes);
	
	@Query(nativeQuery = true ,value = "SELECT m.LOV_TYPE, COUNT(*) FROM MS_FPS_LOV_METADATA m where m.LOV_TYPE IS NOT NULL GROUP BY m.LOV_TYPE ORDER BY UPPER(m.LOV_TYPE)")
	public List<Object[]> findAllLovTypeDetails();
	
	
	@Query(nativeQuery = true, value = "SELECT DISTINCT LOV_CHECK_TYPE FROM MS_FPS_VALIDATION_RULES WHERE BU=:bu AND PLATFORM=:platform AND DATA_CATEGORY=:dataCategory AND DATA_TYPE = 'Dropdown' AND LOV_CHECK_TYPE NOT IN (SELECT DISTINCT LOV_TYPE FROM MS_FPS_LOV_METADATA)")
	public List<String> findDistinctLovTypes(@Param("bu") String bu, @Param("platform") String platform, @Param("dataCategory") String dataCategory);

}
