package com.adp.esi.digitech.ds.config.repo.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.adp.esi.digitech.ds.config.entity.ConfigDataEntity;
import com.adp.esi.digitech.ds.config.model.ConfigurationData;

import jakarta.persistence.criteria.Predicate;

public class ConfigurationDataSpecification {
	
	public static Specification<ConfigDataEntity> hasData(List<ConfigurationData> configurations) {
	    return (root, query, cb) -> {
	        List<Predicate> predicates = new ArrayList<>();
	        for (ConfigurationData configuration : configurations) {
	            predicates.add(cb.and(
	                cb.equal(root.get("bu"), configuration.getBu()),
	                cb.equal(root.get("platform"), configuration.getPlatform()),
	                cb.equal(root.get("dataCategory"), configuration.getDataCategory())
	            ));
	        }
	        return cb.or(predicates.toArray(new Predicate[0]));
	    };
	}

}
