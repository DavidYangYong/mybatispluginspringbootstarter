package com.fl.mybatispluginspringbootstarter.interceptor;

/**
 * @author david
 * @create 2019-07-11 11:46
 **/

import com.fl.mybatispluginspringbootstarter.annotation.CreateTime;
import com.fl.mybatispluginspringbootstarter.annotation.UpdateTime;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

/**
 * 自定义 Mybatis 插件，自动设置 createTime 和 updatTime 的值。 拦截 update 操作（添加和修改）
 */
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class OpertationTimeInterceptor implements Interceptor {

	private Field[] getAllFields(Object object) {
		Class clazz = object.getClass();
		List<Field> fieldList = new ArrayList<>();
		while (clazz != null) {
			fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
			clazz = clazz.getSuperclass();
		}
		Field[] fields = new Field[fieldList.size()];
		fieldList.toArray(fields);
		return fields;
	}

	@Override
	public Object intercept(Invocation invocation) throws Throwable {

		MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];

		// 获取 SQL 命令
		SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
		// 获取参数
		Object parameter = invocation.getArgs()[1];
		if (parameter instanceof MapperMethod.ParamMap) {
			// 拦截方法参数
			MapperMethod.ParamMap method = (MapperMethod.ParamMap) invocation.getArgs()[1];
			Object list = method.get("param1");
			if (list instanceof List) {
				if (list != null) {
					List listClass = (List) list;
					for (Object aClass : listClass) {
						extracted(sqlCommandType, aClass);
					}
				}
			} else {
				extracted(sqlCommandType, parameter);
			}

		} else {
			extracted(sqlCommandType, parameter);
		}

		return invocation.proceed();
	}

	private void extracted(SqlCommandType sqlCommandType, Object parameter) throws IllegalAccessException {
		// 获取私有成员变量
		Field[] declaredFields = getAllFields(parameter);

		for (Field field : declaredFields) {
			if (field.getAnnotation(CreateTime.class) != null) {
				if (SqlCommandType.INSERT.equals(sqlCommandType)) { // insert 语句插入 createTime
					field.setAccessible(true);
					field.set(parameter, new Date());
				}
			}

			if (field.getAnnotation(UpdateTime.class) != null) { // insert 或 update 语句插入 updateTime
				if (SqlCommandType.INSERT.equals(sqlCommandType) || SqlCommandType.UPDATE.equals(sqlCommandType)) {
					field.setAccessible(true);
					field.set(parameter, new Date());
				}
			}
		}
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}
}
