package com.fl.mybatispluginspringbootstarter.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
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
		Date date = parameter.toDate();
		ps.setObject(i, date);
	}

	@Override
	public LocalDate getNullableResult(ResultSet rs, String columnName) throws SQLException {
		Date date = rs.getDate(columnName);
		if (date != null) {
			return new LocalDate(date);
		} else {
			return null;
		}
	}

	@Override
	public LocalDate getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		Date date = rs.getDate(columnIndex);
		if (date != null) {
			return new LocalDate(date);
		} else {
			return null;
		}
	}

	@Override
	public LocalDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		Date date = cs.getDate(columnIndex);
		if (date != null) {
			return new LocalDate(date);
		} else {
			return null;
		}
	}
}
