package com.adp.esi.digitech.ds.config.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.adp.esi.digitech.ds.config.entity.ComplexLOVTypeEntity;

public interface ComplexLovTypeRepository extends JpaRepository<ComplexLOVTypeEntity, Long> {

	@Query(nativeQuery = true, value="SELECT clov.LOV_TYPE, clov.LOV_RECORDS_COUNT, clov.LOV_SCHEMA from MS_FPS_COMPLEX_LOV_METADATA clov where clov.LOV_TYPE IS NOT NULL")
	public List<Object[]> findAllComplexLovTypeDetails();

	public Optional<ComplexLOVTypeEntity> findByLovType(String lovType);
	
}
