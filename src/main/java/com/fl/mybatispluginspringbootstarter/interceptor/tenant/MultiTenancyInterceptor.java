package com.fl.mybatispluginspringbootstarter.interceptor.tenant;

import java.util.List;
import java.util.Properties;
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
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * 共享数据库的多租户系统实现 TODO 白名单模式 TODO 黑名单模式 Created by kleen@qq.com on 2016/08/13.
 */
@Intercepts({
		@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class,
				ResultHandler.class}),
		@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class,
				ResultHandler.class, CacheKey.class, BoundSql.class}),
		@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class MultiTenancyInterceptor implements Interceptor {

	private final static String MANDT = "mandt";

	public MultiTenancyInterceptor() {
	}

	/**
	 *
	 */
	public static class BoundSqlSqlSource implements SqlSource {

		BoundSql boundSql;

		public BoundSqlSqlSource(BoundSql boundSql) {
			this.boundSql = boundSql;
		}

		public BoundSql getBoundSql(Object parameterObject) {
			return boundSql;
		}
	}

	public Object intercept(Invocation invocation) throws Throwable {
		mod(invocation);
		return invocation.proceed();
	}

	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	public void setProperties(Properties properties) {
	}

	/**
	 * 更改MappedStatement为新的
	 */
	public void mod(Invocation invocation) throws Throwable {
		MappedStatement ms = (MappedStatement) invocation
				.getArgs()[0];

		Object parameterObject = null;

		if (invocation.getArgs().length > 1) {
			parameterObject = invocation.getArgs()[1];
		}
		String mandt = "";

		if (parameterObject instanceof MapperMethod.ParamMap) {
			ParamMap map = (ParamMap) parameterObject;
			for (Object o : map.keySet()) {
				Object param = map.get(o);
				if (param instanceof ITenantInfo) {
					ITenantInfo tenantInfo = (ITenantInfo) param;
					if (tenantInfo != null) {
						mandt = tenantInfo.getMandt();
					}
				}
			}
		}
		BoundSql boundSql = ms.getBoundSql(parameterObject);
		/**
		 * 根据已有BoundSql构造新的BoundSql
		 *
		 */
		BoundSql newBoundSql = new BoundSql(
				ms.getConfiguration(),
				addWhere(mandt, boundSql.getSql()),//更改后的sql
				boundSql.getParameterMappings(),
				boundSql.getParameterObject());
		/**
		 * 根据已有MappedStatement构造新的MappedStatement
		 *
		 */
		MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(),
				ms.getId(),
				new BoundSqlSqlSource(newBoundSql),
				ms.getSqlCommandType());

		builder.resource(ms.getResource());
		builder.fetchSize(ms.getFetchSize());
		builder.statementType(ms.getStatementType());
		builder.keyGenerator(ms.getKeyGenerator());
		builder.timeout(ms.getTimeout());
		builder.parameterMap(ms.getParameterMap());
		builder.resultMaps(ms.getResultMaps());
		builder.cache(ms.getCache());
		MappedStatement newMs = builder.build();
		/**
		 * 替换MappedStatement
		 */
		invocation.getArgs()[0] = newMs;
	}

	/**
	 * 添加租户id条件
	 */
	private String addWhere(String mandt, String sql) throws JSQLParserException {
		Statement stmt = CCJSqlParserUtil.parse(sql);

		if (StringUtils.isNotEmpty(mandt)) {
			if (stmt instanceof Update) {
				//获得Update对象
				Update updateStatement = (Update) stmt;
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

			if (stmt instanceof Select) {
				Select select = (Select) stmt;
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
		throw new RuntimeException("非法sql语句,请检查" + sql);

	}
}
