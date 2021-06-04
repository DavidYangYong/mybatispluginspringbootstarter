package com.fl.mybatispluginspringbootstarter.type;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.joda.time.LocalDateTime;

/**
 * @author david
 * @create 2021-06-04 09:22
 **/
public class LocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

	public LocalDateTimeTypeHandler() {
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType)
			throws SQLException {
		ps.setDate(i, new Date(parameter.toDateTime()
		                                .toDate()
		                                .getTime()));
	}

	@Override
	public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return (LocalDateTime) rs.getObject(columnName, LocalDateTime.class);
	}

	@Override
	public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return (LocalDateTime) rs.getObject(columnIndex, LocalDateTime.class);
	}

	@Override
	public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return (LocalDateTime) cs.getObject(columnIndex, LocalDateTime.class);
	}
}
