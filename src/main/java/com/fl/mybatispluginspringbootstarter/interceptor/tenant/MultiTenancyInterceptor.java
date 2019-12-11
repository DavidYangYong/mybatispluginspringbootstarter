package com.fl.mybatispluginspringbootstarter.interceptor.tenant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * 共享数据库的多租户系统实现 TODO 白名单模式 TODO 黑名单模式 Created by kleen@qq.com on 2016/08/13.
 */
@Intercepts({
		@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class,
				ResultHandler.class, CacheKey.class, BoundSql.class}),
		@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class,
				ResultHandler.class}),
		@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class MultiTenancyInterceptor implements Interceptor {

	private final static String MANDT = "MANDT";

	public MultiTenancyInterceptor() {
	}

	@Override
	public Object intercept(Invocation invocation) throws Throwable {

		return mod(invocation);
	}

	public static class BoundSqlSqlSource implements SqlSource {

		BoundSql boundSql;

		public BoundSqlSqlSource(BoundSql boundSql) {
			this.boundSql = boundSql;
		}

		@Override
		public BoundSql getBoundSql(Object parameterObject) {
			return boundSql;
		}
	}

	@Override
	public void setProperties(Properties properties) {
	}

	boolean resolve(MetaObject mo) {
		String originalSql = (String) mo.getValue("boundSql.sql");
		MappedStatement ms = (MappedStatement) mo.getValue("mappedStatement");
		if (ms.getSqlCommandType() == SqlCommandType.SELECT || ms.getSqlCommandType() == SqlCommandType.UPDATE) {
			// sql中包含mandt = ?
			return Pattern.matches("[\\s\\S]*?" + MANDT + "[\\s\\S]*?=[\\s\\S]*?\\?[\\s\\S]*?", originalSql.toLowerCase());
		}
		return false;
	}

	@Override
	public Object plugin(Object target) {
		if (target instanceof Executor || target instanceof StatementHandler) {
			return Plugin.wrap(target, this);
		}
		return target;
	}

	/**
	 * 添加租户id条件
	 */
	private String addWhere(String mandt, String sql, MappedStatement mappedStatement) throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse(sql);

		if (StringUtils.isNotEmpty(mandt)) {
			if (SqlCommandType.UPDATE == mappedStatement.getSqlCommandType()) {
				//获得Update对象
				Update updateStatement = (Update) statement;
				//获得where条件表达式
				Expression where = updateStatement.getWhere();
				if (where instanceof BinaryExpression) {
					EqualsTo equalsTo = new EqualsTo();
					equalsTo.setLeftExpression(new Column(MANDT));
					equalsTo.setRightExpression(new StringValue(mandt));
					AndExpression andExpression = new AndExpression(equalsTo, where);
					updateStatement.setWhere(andExpression);
				}
				return updateStatement.toString();
			}

			if (SqlCommandType.SELECT == mappedStatement.getSqlCommandType()) {
				Select select = (Select) statement;
				PlainSelect ps = (PlainSelect) select.getSelectBody();
				TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
				List<String> tableList = tablesNamesFinder.getTableList(select);
				Map<String, String> map = parseTableAlias(ps);
				for (String table : tableList) {
					EqualsTo equalsTo = new EqualsTo();
					equalsTo.setLeftExpression(new Column(map.get(table) + '.' + MANDT));
					equalsTo.setRightExpression(new StringValue(mandt));
					if (ps.getWhere() == null) {

						ps.setWhere(new Parenthesis(equalsTo));

					} else {

						ps.setWhere(new AndExpression(ps.getWhere(), new Parenthesis(equalsTo)));

					}
				}
				return select.toString();
			}
		}
		return sql;
	}

	public Map<String, String> parseTableAlias(PlainSelect plainSelect) {
		Map<String, String> map = new HashMap<>();
		Table table = (Table) plainSelect.getFromItem();
		if (table.getAlias() != null) {
			map.put(table.getFullyQualifiedName(), table.getAlias().getName());
		} else {
			map.put(table.getFullyQualifiedName(), table.getName());
		}

		if (plainSelect.getJoins() != null && plainSelect.getJoins().size() > 0) {
			for (Join join : plainSelect.getJoins()) {
				Table table1 = (Table) join.getRightItem();
				if (table1.getAlias() != null) {
					map.put(table1.getFullyQualifiedName(), table1.getAlias().getName());
				} else {
					map.put(table1.getFullyQualifiedName(), table1.getName());
				}
			}
		}
		return map;
	}

	/**
	 * 更改MappedStatement为新的
	 */
	public Object mod(Invocation invocation) throws Throwable {

		MappedStatement ms = (MappedStatement) invocation
				.getArgs()[0];

		Object parameterObject = null;

		if (invocation.getArgs().length > 1) {
			parameterObject = invocation.getArgs()[1];
		}

		String mandt = "";
		//获取所有参数
		if (parameterObject instanceof ITenantInfo) {
			if (parameterObject != null) {
				ITenantInfo tenantInfo = (ITenantInfo) parameterObject;
				if (tenantInfo != null) {
					mandt = tenantInfo.getMandt();
				}
			}
		} else if (parameterObject instanceof MapperMethod.ParamMap) {
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
				addWhere(mandt, boundSql.getSql(), ms),//更改后的sql
				boundSql.getParameterMappings(),
				boundSql.getParameterObject());
		/**
		 * 根据已有MappedStatement构造新的MappedStatement
		 *
		 */
		// 把新的查询放到statement里
		MappedStatement newMs = copyFromMappedStatement(ms, new BoundSqlSqlSource(newBoundSql));
		for (ParameterMapping mapping : boundSql.getParameterMappings()) {
			String prop = mapping.getProperty();
			if (boundSql.hasAdditionalParameter(prop)) {
				newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
			}
		}
		/**
		 * 替换MappedStatement
		 */
		invocation.getArgs()[0] = newMs;

		return invocation.proceed();
	}

	private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
		MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource,
				ms.getSqlCommandType());
		builder.resource(ms.getResource());
		builder.fetchSize(ms.getFetchSize());
		builder.statementType(ms.getStatementType());
		builder.keyGenerator(ms.getKeyGenerator());
		if (ms.getKeyProperties() != null && ms.getKeyProperties().length > 0) {
			builder.keyProperty(ms.getKeyProperties()[0]);
		}
		builder.timeout(ms.getTimeout());
		builder.parameterMap(ms.getParameterMap());
		builder.resultMaps(ms.getResultMaps());
		builder.resultSetType(ms.getResultSetType());
		builder.cache(ms.getCache());
		builder.flushCacheRequired(ms.isFlushCacheRequired());
		builder.useCache(ms.isUseCache());
		return builder.build();
	}
}
