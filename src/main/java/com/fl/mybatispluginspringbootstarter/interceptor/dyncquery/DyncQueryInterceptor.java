package com.fl.mybatispluginspringbootstarter.interceptor.dyncquery;

/**
 * @author david
 * @date
 **/

import com.fl.mybatispluginspringbootstarter.dto.search.DyncQueryBase;
import com.fl.mybatispluginspringbootstarter.interceptor.tenant.MultiTenancyInterceptor.BoundSqlSqlSource;
import com.fl.mybatispluginspringbootstarter.utils.SQLHelpTool;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
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
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * 自定义 Mybatis 插件，自动设置过滤查询条件
 */
@Intercepts({
		@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class,
				ResultHandler.class, CacheKey.class, BoundSql.class}),
		@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class,
				ResultHandler.class})
})
@Slf4j
public class DyncQueryInterceptor implements Interceptor {


	@Override
	public Object intercept(Invocation invocation) throws Throwable {

		MappedStatement ms = (MappedStatement) invocation
				.getArgs()[0];

		SqlCommandType sqlCommandType = ms.getSqlCommandType();
		log.debug("sqlCommandType is: {} ", sqlCommandType.name());
		if (SqlCommandType.SELECT == sqlCommandType) {
			{

				Object parameterObject = null;

				if (invocation.getArgs().length > 1) {
					parameterObject = invocation.getArgs()[1];
				}

				DyncQueryBase dyncQueryBase = null;
				//获取所有参数
				if (parameterObject instanceof DyncQueryBase) {
					if (parameterObject != null) {
						dyncQueryBase = (DyncQueryBase) parameterObject;
					}
				} else if (parameterObject instanceof MapperMethod.ParamMap) {
					ParamMap map = (ParamMap) parameterObject;
					for (Object o : map.keySet()) {
						Object param = map.get(o);
						if (param instanceof DyncQueryBase) {
							dyncQueryBase = (DyncQueryBase) param;
						}
					}
				}

				BoundSql boundSql = ms.getBoundSql(parameterObject);

				String oldSql = boundSql.getSql();

				log.debug("oldsql is {} ", oldSql);
				/**
				 * 根据已有BoundSql构造新的BoundSql
				 *
				 */
				BoundSql newBoundSql = new BoundSql(
						ms.getConfiguration(),
						addWhere(oldSql, ms, dyncQueryBase),//更改后的sql
						boundSql.getParameterMappings(),
						boundSql.getParameterObject());
				/**
				 * 根据已有MappedStatement构造新的MappedStatement
				 *
				 */
				// 把新的查询放到statement里
				MappedStatement newms = copyFromMappedStatement(ms, new BoundSqlSqlSource(newBoundSql));
				for (ParameterMapping mapping : boundSql.getParameterMappings()) {
					String prop = mapping.getProperty();
					if (boundSql.hasAdditionalParameter(prop)) {
						newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
					}
				}
				/**
				 * 替换MappedStatement
				 */
				invocation.getArgs()[0] = newms;
				return invocation.proceed();
			}
		}
		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}

	private String addWhere(String sql, MappedStatement mappedStatement, DyncQueryBase dyncQueryBase) throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse(sql);
		SQLHelpTool sqlHelpTool = new SQLHelpTool();

		if (dyncQueryBase != null) {
			if (SqlCommandType.SELECT == mappedStatement.getSqlCommandType()) {
				Select select = (Select) statement;
				SelectBody selectBody = select.getSelectBody();
				if (selectBody instanceof PlainSelect) {
					PlainSelect ps = (PlainSelect) selectBody;
					Expression expression = sqlHelpTool.getSqlExpression(dyncQueryBase);
					if (ps.getWhere() == null) {
						ps.setWhere(new Parenthesis(expression));
					} else {
						ps.setWhere(new AndExpression(ps.getWhere(), new Parenthesis(expression)));
					}
					return select.toString();
				}

			}
		}
		return sql;
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
