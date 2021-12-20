package com.fl.mybatispluginspringbootstarter.dto.search;

import java.util.List;

/**
 * @author david
 * @create 2021-07-23 08:32
 **/
public abstract class DyncQueryBase<T> {

	private List<FilterDto> filterList;
	private T queryObj;

	public List<FilterDto> getFilterList() {
		return filterList;
	}

	public void setFilterList(List<FilterDto> filterList) {
		this.filterList = filterList;
	}

	public abstract T getEntity();

	public void setQueryObj(T queryObj) {
		this.queryObj = queryObj;
	}

}
