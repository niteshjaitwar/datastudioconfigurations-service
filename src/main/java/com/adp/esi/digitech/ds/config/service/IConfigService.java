package com.adp.esi.digitech.ds.config.service;

import java.util.List;
import java.util.Map;

public interface IConfigService<T> {

	public List<T> findAll();
	
	public T findById(Long id);
	
	public T saveEntity(T data);
	
	public List<T> saveEntities(List<T> data);
	
	public T updateSingle(T data);
	
	public List<T> updateBulk(List<T> data);
	
	List<T> patch(List<Map<String, String>> configurations);
	
	public void deleteSingle(Long id);
	
	public void deleteBulk(List<Long> ids);
}
