package com.fl.mybatispluginspringbootstarter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by Administrator on 2018/12/20 0020.
 *
 * @author even
 */
@ConfigurationProperties(prefix = "com.fl.mybatis.multi-tenancy")
public class FlMybatisProperties {

	private boolean enabled = Boolean.FALSE;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
