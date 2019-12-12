//package com.fl;
//
//import java.io.StringReader;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import net.sf.jsqlparser.JSQLParserException;
//import net.sf.jsqlparser.expression.Expression;
//import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
//import net.sf.jsqlparser.parser.CCJSqlParserManager;
//import net.sf.jsqlparser.parser.CCJSqlParserUtil;
//import net.sf.jsqlparser.schema.Column;
//import net.sf.jsqlparser.statement.select.PlainSelect;
//import net.sf.jsqlparser.statement.select.Select;
//import net.sf.jsqlparser.statement.select.SelectExpressionItem;
//import net.sf.jsqlparser.statement.select.SelectItem;
//import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;
//import org.apache.commons.collections4.CollectionUtils;
//
///**
// * @author david
// * @create 2019-12-12 10:36
// **/
//
//public class Test {
//
//	public static void main(String[] args) throws JSQLParserException {
//
//		String string = "SELECT col1 AS a, col2 AS b, col3 AS c FROM table WHERE col1 = 10 AND col2 = 20 AND col3 = 30";
//		System.out.println(test_select_where(string));
//		testCurrentDateExpression(string);
//	}
//
//	public void test1() throws JSQLParserException {
//		Select stmt = (Select) CCJSqlParserUtil
//				.parse("SELECT col1 AS a, col2 AS b, col3 AS c FROM table WHERE col1 = 10 AND col2 = 20 AND col3 = 30");
//
//		Map<String, Expression> map = new HashMap<>();
//		for (SelectItem selectItem : ((PlainSelect) stmt.getSelectBody()).getSelectItems()) {
//			selectItem.accept(new SelectItemVisitorAdapter() {
//				@Override
//				public void visit(SelectExpressionItem item) {
//					map.put(item.getAlias().getName(), item.getExpression());
//				}
//			});
//		}
//
//		System.out.println("map " + map);
//	}
//
//	public static String test_select_where(String sql)
//			throws JSQLParserException {
//		CCJSqlParserManager parserManager = new CCJSqlParserManager();
//		Select select = (Select) parserManager.parse(new StringReader(sql));
//		PlainSelect plain = (PlainSelect) select.getSelectBody();
//		Expression where_expression = plain.getWhere();
//		String str = where_expression.toString();
//		return str;
//	}
//
//	public static void testCurrentDateExpression(String sql) throws JSQLParserException {
//		final List<String> columnList = new ArrayList<String>();
//		Select select = (Select) CCJSqlParserUtil.
//				parse(sql);
//		PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
//		Expression where = plainSelect.getWhere();
//		where.accept(new ExpressionVisitorAdapter() {
//
//			@Override
//			public void visit(Column column) {
//				super.visit(column);
//				columnList.add(column.getColumnName().toUpperCase());
//			}
//		});
//
//		boolean bv = CollectionUtils.containsAny(columnList, "COL1");
//		System.out.println(bv);
//	}
//
//}
