package com.fl.mybatispluginspringbootstarter.autoconfigure;

/**
 * 自定注入createTime and updateTime 插件
 *
 * @author david
 * @create 2019-07-11 11:43
 **/

import com.fl.mybatispluginspringbootstarter.interceptor.OpertationTimeInterceptor;
import com.fl.mybatispluginspringbootstarter.interceptor.tenant.MultiTenancyInterceptor;
import com.fl.mybatispluginspringbootstarter.properties.FlMybatisProperties;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(SqlSessionFactory.class)
@AutoConfigureAfter(PageHelperAutoConfiguration.class)
public class MybatisPluginAutoConfiguration {

	@Autowired
	private List<SqlSessionFactory> sqlSessionFactoryList;


	@Autowired
	private FlMybatisProperties flMybatisProperties;

	@PostConstruct
	public void addPageInterceptor() {
		OpertationTimeInterceptor interceptor = new OpertationTimeInterceptor();
		MultiTenancyInterceptor multiTenancyInterceptor = new MultiTenancyInterceptor();
		boolean b = flMybatisProperties.isEnabled();
		for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
			sqlSessionFactory.getConfiguration().addInterceptor(interceptor);
			if (b) {
				sqlSessionFactory.getConfiguration().addInterceptor(multiTenancyInterceptor);
			}
		}
	}

}
