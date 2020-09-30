package com.fl.mybatispluginspringbootstarter.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by Administrator on 2018/12/20 0020.
 *
 * @author even
 */
@ConfigurationProperties(prefix = "com.fl.mybatis.multi-tenancy", ignoreInvalidFields = true)
public class FlMybatisProperties {

	@Value("${com.fl.mybatis.multi-tenancy.enabled:false}")
	private boolean enabled;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
