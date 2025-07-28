package com.adp.esi.digitech.ds.config.repo.specification;

import org.springframework.data.jpa.domain.Specification;

import com.adp.esi.digitech.ds.config.entity.FileSchemaConfigurationEntity;

public class FileSchemaConfigurationSpecification {
	
	public static Specification<FileSchemaConfigurationEntity> hasBu(String bu) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("bu"), bu);
	}
	
	public static Specification<FileSchemaConfigurationEntity> hasPlatform(String platform) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("platform"), platform);
	}
	
	public static Specification<FileSchemaConfigurationEntity> hasDataCategory(String dataCategory) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("dataCategory"), dataCategory);
	}
	
	public static Specification<FileSchemaConfigurationEntity> hasSourceKey(String sourceKey) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("sourceKey"), sourceKey);
	}
	
	public static Specification<FileSchemaConfigurationEntity> hasVersion(String version) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("version"), version);
	}

}
