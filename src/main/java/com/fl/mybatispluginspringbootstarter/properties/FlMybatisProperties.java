package com.fl.mybatispluginspringbootstarter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by Administrator on 2018/12/20 0020.
 *
 * @author even
 */
@ConfigurationProperties(prefix = "com.fl.mybatis")
public class FlMybatisProperties {

	private boolean multiTenancy = Boolean.FALSE;
	private boolean dyncQuery = Boolean.FALSE;

	public boolean isDyncQuery() {
		return dyncQuery;
	}

	public void setDyncQuery(boolean dyncQuery) {
		this.dyncQuery = dyncQuery;
	}

	public boolean isMultiTenancy() {
		return multiTenancy;
	}

	public void setMultiTenancy(boolean multiTenancy) {
		this.multiTenancy = multiTenancy;
	}
}
