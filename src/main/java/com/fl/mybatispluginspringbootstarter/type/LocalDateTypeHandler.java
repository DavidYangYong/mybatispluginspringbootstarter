package com.fl.mybatispluginspringbootstarter.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.joda.time.LocalDate;

/**
 * @author david
 * @create 2021-06-04 09:25
 **/
public class LocalDateTypeHandler extends BaseTypeHandler<LocalDate> {

	public LocalDateTypeHandler() {
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, LocalDate parameter, JdbcType jdbcType)
			throws SQLException {
		ps.setObject(i, parameter);
	}

	@Override
	public LocalDate getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return (LocalDate) rs.getObject(columnName, LocalDate.class);
	}

	@Override
	public LocalDate getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return (LocalDate) rs.getObject(columnIndex, LocalDate.class);
	}

	@Override
	public LocalDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return (LocalDate) cs.getObject(columnIndex, LocalDate.class);
	}
}
