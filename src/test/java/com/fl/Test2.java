package com.fl;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;

/**
 * @author david
 * @create 2020-02-19 17:18
 **/
public class Test2 {

	public static void main(String[] args) throws JSQLParserException {
		String sql = "select sec.salesempinfo_SEQ.NEXTVAL as id from DUAL";
		Statement statement = CCJSqlParserUtil.parse(sql);
		Select select = (Select) statement;
		SelectBody selectBody = select.getSelectBody();
		System.out.println(selectBody);

	}
}
