package com.fl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jsqlparser.JSQLParserException;
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
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.lang3.StringUtils;

/**
 * @author david
 * @create 2020-02-26 19:11
 **/
public class Test3 {

	private final static String MANDT = "MANDT";

	public static void main(String[] args) throws JSQLParserException {
		String sql = "SELECT\n"
				+ "         \n"
				+ "        ami.ID,\n"
				+ "        ami.PID,\n"
				+ "        ami.NAME,\n"
				+ "        ami.NAME name2,\n"
				+ "        (select name from au.menu_items where id = ami.PID) parentName,\n"
				+ "        ami.TITLE,\n"
				+ "        ami.DESCRIPTION,\n"
				+ "        ami.URL,\n"
				+ "        ami.SYSID,\n"
				+ "        ami.TARGET target1,\n"
				+ "        case when ami.TARGET='DIR' then '文件夹' when ami.TARGET='MAIN' then '节点' else '' end TARGET,\n"
				+ "        ami.ICON,\n"
				+ "        ami.ICON_OPEN iconOpen,\n"
				+ "        ami.ALL_VIEW allView,\n"
				+ "        ami.ORDER_BY orderBy,\n"
				+ "        ami.ISKUNNR_MENU isKunnrMenu,\n"
				+ "        ami.ISOFFICE_MENU isOfficeMenu,\n"
				+ "        ami.ISCLIENT_MENU isClientMenu,\n"
				+ "        ami.ISCUST_MENU isCustMenu,\n"
				+ "        ami.REDIRECT_URL redirectUrl,\n"
				+ "        ami.QUERYURL,\n"
				+ "        ami.CREATE_BY createBy,\n"
				+ "        ami.STATUS,\n"
				+ "        ami.VERSION,\n"
				+ "        to_char(ami.CREATE_DATE,'YYYY-MM-DD HH24:MI:SS.FF') createDate\n"
				+ "     \n"
				+ "        FROM menu_items ami\n"
				+ "        where ami.status != '003'\n"
				+ "        AND ami.sysId = 'AU'\n"
				+ "         \n"
				+ "         \n"
				+ "        ORDER BY ami.id ";
		String newsql = test(sql, "000");
		System.out.println(newsql);
	}

	public static Map<String, String> parseTableAlias(PlainSelect plainSelect) {
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

	private static String test(String sql, String mandt) throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse(sql);

		Select select = (Select) statement;
		SelectBody selectBody = select.getSelectBody();
		if (selectBody instanceof PlainSelect) {
			PlainSelect ps = (PlainSelect) selectBody;
			TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
			List<String> tableList = tablesNamesFinder.getTableList(select);
			Map<String, String> map = parseTableAlias(ps);
			for (String table : tableList) {
				if (StringUtils.isNotEmpty(map.get(table))) {
					EqualsTo equalsTo = new EqualsTo();
					equalsTo.setLeftExpression(new Column(map.get(table) + '.' + MANDT));
					equalsTo.setRightExpression(new StringValue(mandt));
					if (ps.getWhere() == null) {

						ps.setWhere(new Parenthesis(equalsTo));

					} else {

						ps.setWhere(new AndExpression(ps.getWhere(), new Parenthesis(equalsTo)));

					}
				}
			}
			return select.toString();
		}
		return sql;
	}
}
