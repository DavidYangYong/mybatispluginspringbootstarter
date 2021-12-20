package com.fl.mybatispluginspringbootstarter.utils;

import com.fl.mybatispluginspringbootstarter.dto.search.DyncQueryBase;
import com.fl.mybatispluginspringbootstarter.dto.search.FilterDto;
import com.fl.mybatispluginspringbootstarter.dto.search.QueryFilterTarget;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * @desc: 条件查询sql拼接
 * @auther: Sakura
 * @date: 16:50
 */
@Slf4j
public class SQLHelpTool {

	public QueryFilterTarget queryFilterName(DyncQueryBase dyncQueryBase, List<Field> fieldsWithAnnotation,
			String fieldName) {
		QueryFilterTarget myTarget = null;
		Collection<Field> list = CollectionUtils.select(fieldsWithAnnotation, new Predicate<Field>() {
			@Override
			public boolean evaluate(Field field) {
				if (StringUtils.equals(fieldName, field.getName())) {
					return true;
				}
				return false;
			}
		});
		if (CollectionUtils.isNotEmpty(list)) {
			Field field = list.stream().findFirst().get();
			myTarget = field.getAnnotation(QueryFilterTarget.class);
		}
		return myTarget;
	}

	public String getSql(DyncQueryBase dyncQueryBase) {
		Expression expression = getSqlExpression(dyncQueryBase);
		if (expression != null) {
			return expression.toString();
		}
		return "";
	}

	public Expression getSqlExpression(DyncQueryBase dyncQueryBase) {
		List<FilterDto> list = dyncQueryBase.getFilterList();
		if (CollectionUtils.isNotEmpty(list)) {
			List<Field> fieldsWithAnnotation = FieldUtils.getFieldsListWithAnnotation(dyncQueryBase.getEntity().getClass(),
					QueryFilterTarget.class);
			PlainSelect plainSelect = new PlainSelect();
			for (int i = 0; i < list.size(); i++) {
				FilterDto filterDto = list.get(i);
				String flg = filterDto.getConstraint();
				Between between = null;
				InExpression inExpression = null;
				BinaryExpression whereExpression = null;
				List<Expression> expressions = null;
				String[] strings = null;
				MultiExpressionList multiExpressionList = null;
				String filerName = "";
				String tableName = "";
				QueryFilterTarget queryFilterTarget = queryFilterName(dyncQueryBase, fieldsWithAnnotation, filterDto.getField());
				if (queryFilterTarget != null) {
					filerName = queryFilterTarget.name();
					tableName = queryFilterTarget.tableName();
				}
				if (StringUtils.isNotEmpty(filerName) && StringUtils.isNotEmpty(filterDto.getValue())) {
					String columnName = StringUtils.join(tableName, ".", filerName);
					Column column = new Column(columnName);
					switch (flg) {
						case "equal":
							whereExpression = new EqualsTo();
							whereExpression.setLeftExpression(column);
							whereExpression.setRightExpression(new StringValue(filterDto.getValue()));
							break;
						case "unequal":
							whereExpression = new NotEqualsTo("!=");
							whereExpression.setLeftExpression(column);
							whereExpression.setRightExpression(new StringValue(filterDto.getValue()));
							break;
						case "between":
							between = new Between();
							between.setLeftExpression(column);
							between.setBetweenExpressionStart(new StringValue(filterDto.getCondStart()));
							between.setBetweenExpressionEnd(new StringValue(filterDto.getCondEnd()));
							break;
						case "in":
							inExpression = getInExpression(filterDto, column, false);
							break;
						case "notin":
							inExpression = getInExpression(filterDto, column, true);
							break;
						default:
							break;
					}
					if (plainSelect.getWhere() == null) {
						if (between != null) {
							plainSelect.setWhere(between);
						}
						if (whereExpression != null) {
							plainSelect.setWhere(whereExpression);
						}
						if (inExpression != null) {
							plainSelect.setWhere(inExpression);
						}
					} else {
						if (between != null) {
							plainSelect.setWhere(new AndExpression(plainSelect.getWhere(), between));
						}
						if (whereExpression != null) {
							plainSelect.setWhere(new AndExpression(plainSelect.getWhere(), whereExpression));
						}
						if (inExpression != null) {
							plainSelect.setWhere(new AndExpression(plainSelect.getWhere(), inExpression));
						}
					}
				}

			}
			return plainSelect.getWhere();
		} else {
			return null;
		}
	}

	private InExpression getInExpression(FilterDto filterDto, Column column, boolean not) {
		InExpression inExpression;
		MultiExpressionList multiExpressionList;
		List<Expression> expressions;
		String[] strings;
		inExpression = new InExpression();
		inExpression.setLeftExpression(column);
		expressions = new ArrayList<Expression>();
		strings = StringUtils.split(filterDto.getValue(), ",");
		for (int i = 0; i < strings.length; i++) {
			Expression expression = new StringValue(strings[i]);
			expressions.add(expression);
		}
		multiExpressionList = new MultiExpressionList();
		multiExpressionList.addExpressionList(expressions);
		if (not) {
			inExpression.setNot(not);
		}
		inExpression.setRightItemsList(multiExpressionList);
		return inExpression;
	}
}
