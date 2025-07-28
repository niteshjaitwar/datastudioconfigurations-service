package com.adp.esi.digitech.ds.config.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Values<T> {

	private List<T> values;
}
