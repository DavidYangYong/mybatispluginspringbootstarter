package com.fl.mybatispluginspringbootstarter.autoconfigure;

/**
 * 自定注入createTime and updateTime 插件
 *
 * @author david
 * @create 2019-07-11 11:43
 **/

import com.fl.mybatispluginspringbootstarter.interceptor.OpertationTimeInterceptor;
import com.fl.mybatispluginspringbootstarter.interceptor.dyncquery.DyncQueryInterceptor;
import com.fl.mybatispluginspringbootstarter.interceptor.tenant.MultiTenancyInterceptor;
import com.fl.mybatispluginspringbootstarter.properties.FlMybatisProperties;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(SqlSessionFactory.class)
@AutoConfigureAfter(PageHelperAutoConfiguration.class)
@EnableConfigurationProperties({FlMybatisProperties.class})
public class FlMybatisPluginAutoConfiguration {

	@Autowired
	private List<SqlSessionFactory> sqlSessionFactoryList;

	@Autowired(required = false)
	private FlMybatisProperties flMybatisProperties;

	public FlMybatisPluginAutoConfiguration() {
	}

	public FlMybatisProperties getFlMybatisProperties() {
		return flMybatisProperties;
	}

	@PostConstruct
	public void addPageInterceptor() {
		OpertationTimeInterceptor interceptor = new OpertationTimeInterceptor();
		for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
			sqlSessionFactory.getConfiguration().addInterceptor(interceptor);

			if (flMybatisProperties != null && flMybatisProperties.isMultiTenancy()) {
				MultiTenancyInterceptor multiTenancyInterceptor = new MultiTenancyInterceptor();
				sqlSessionFactory.getConfiguration().addInterceptor(multiTenancyInterceptor);
			}
			if (flMybatisProperties != null && flMybatisProperties.isDyncQuery()) {
				DyncQueryInterceptor dyncQueryInterceptor = new DyncQueryInterceptor();
				sqlSessionFactory.getConfiguration().addInterceptor(dyncQueryInterceptor);
			}
		}
	}

}
