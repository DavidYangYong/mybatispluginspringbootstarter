package com.fl.mybatispluginspringbootstarter.interceptor.tenant;

import com.fl.mybatispluginspringbootstarter.utils.PluginUtils;
import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

/**
 * 共享数据库的多租户系统实现 TODO 白名单模式 TODO 黑名单模式 Created by kleen@qq.com on 2016/08/13.
 */
@Intercepts({
		@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
		@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class MultiTenancyInterceptor implements Interceptor {

	private final static String MANDT = "MANDT";

	public MultiTenancyInterceptor() {
	}

	public Object intercept(Invocation invocation) throws Throwable {

		return mod(invocation);
	}

	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	public void setProperties(Properties properties) {
	}

	boolean resolve(MetaObject mo) {
		String originalSql = (String) mo.getValue("boundSql.sql");
		MappedStatement ms = (MappedStatement) mo.getValue("mappedStatement");
		if (Objects.equals(ms.getSqlCommandType(), SqlCommandType.UPDATE)) {
			// sql中包含mandt = ?
			return Pattern.matches("[\\s\\S]*?" + MANDT + "[\\s\\S]*?=[\\s\\S]*?\\?[\\s\\S]*?", originalSql.toLowerCase());
		}
		return false;
	}

	/**
	 * 更改MappedStatement为新的
	 */
	public Object mod(Invocation invocation) throws Throwable {
		String interceptMethod = invocation.getMethod().getName();
		StatementHandler statementHandler = (StatementHandler) PluginUtils.realTarget(invocation.getTarget());
		MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
		// 先判断是不是SELECT操作
		MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
		if (SqlCommandType.SELECT != mappedStatement.getSqlCommandType()
				|| StatementType.CALLABLE == mappedStatement.getStatementType()) {
			return invocation.proceed();
		}
		BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");

		if ("prepare".equals(interceptMethod)) {
			String mandt = "";
			//获取所有参数
			List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
			if (parameterMappings != null) {
				for (int i = 0; i < parameterMappings.size(); i++) {
					ParameterMapping parameterMapping = parameterMappings.get(i);
					if (parameterMapping instanceof ITenantInfo) {
						mandt = ((ITenantInfo) parameterMapping).getMandt();
					}
				}
			}

			String originalSql = boundSql.getSql();
			String builder = addWhere(mandt, originalSql);
			metaObject.setValue("delegate.boundSql.sql", builder);
		} else if ("update".equals(interceptMethod)) {

			MappedStatement ms = (MappedStatement) invocation
					.getArgs()[0];

			Object parameterObject = null;

			if (invocation.getArgs().length > 1) {
				parameterObject = invocation.getArgs()[1];
			}
			String mandt = "";
			if (parameterObject instanceof ITenantInfo) {
				mandt = ((ITenantInfo) parameterObject).getMandt();
				String originalSql = boundSql.getSql();
				String builder = addWhere(mandt, originalSql);
				metaObject.setValue("delegate.boundSql.sql", builder);
			}
		}
		return invocation.proceed();
	}

	/**
	 * 添加租户id条件
	 */
	private String addWhere(String mandt, String sql) throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse(sql);

		if (StringUtils.isNotEmpty(mandt)) {
			if (statement instanceof Update) {
				//获得Update对象
				Update updateStatement = (Update) statement;
				//获得where条件表达式
				Expression where = updateStatement.getWhere();
				if (where instanceof BinaryExpression) {
					EqualsTo equalsTo = new EqualsTo();
					equalsTo.setLeftExpression(new Column(MANDT));
					equalsTo.setRightExpression(new StringValue("," + mandt + ","));
					AndExpression andExpression = new AndExpression(equalsTo, where);
					updateStatement.setWhere(andExpression);
				}
				return updateStatement.toString();
			}

			if (statement instanceof Select) {
				Select select = (Select) statement;
				PlainSelect ps = (PlainSelect) select.getSelectBody();
				TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
				List<String> tableList = tablesNamesFinder.getTableList(select);
				if (tableList.size() > 1) {
					return select.toString();
				}
				for (String table : tableList) {
					EqualsTo equalsTo = new EqualsTo();
					equalsTo.setLeftExpression(new Column(table + '.' + MANDT));
					equalsTo.setRightExpression(new StringValue("," + mandt + ","));
					AndExpression andExpression = new AndExpression(equalsTo, ps.getWhere());
					ps.setWhere(andExpression);
				}
				return select.toString();
			}
		}
		return sql;
	}
}
