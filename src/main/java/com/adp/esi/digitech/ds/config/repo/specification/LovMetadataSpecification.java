package com.adp.esi.digitech.ds.config.repo.specification;

import org.springframework.data.jpa.domain.Specification;

import com.adp.esi.digitech.ds.config.entity.LOVTypeEntity;

public class LovMetadataSpecification {

	public static Specification<LOVTypeEntity> hasLovType(String lovType) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("lovType"), lovType);
	}

	public static Specification<LOVTypeEntity> hasLovName(String lovName) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("lovName"), "%" + lovName + "%");
	}

	public static Specification<LOVTypeEntity> hasLovValue(String lovValue) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("lovValue"), "%" + lovValue + "%");
	}
}
