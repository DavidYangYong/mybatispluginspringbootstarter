package com.fl.mybatispluginspringbootstarter.dto.search;

public class FilterDto {

	/**
	 * 字段描述
	 */
	private String fieldName;
	/**
	 * 字段
	 */
	private String field;
	/**
	 * 条件描述
	 */
	private String constraintName;
	/**
	 * 条件
	 */
	private String constraint;
	/**
	 * 单值
	 */
	private String value;
	/**
	 * 范围起始值
	 */
	private String condStart;
	/**
	 * 范围结束值
	 */
	private String condEnd;

	private String condType;

	public String getCondType() {
		return condType;
	}

	public void setCondType(String condType) {
		this.condType = condType;
	}

	@Override
	public String toString() {
		return "FilterDto{" +
				"fieldName='" + fieldName + '\'' +
				", field='" + field + '\'' +
				", constraintName='" + constraintName + '\'' +
				", constraint='" + constraint + '\'' +
				", value='" + value + '\'' +
				", condStart='" + condStart + '\'' +
				", condEnd='" + condEnd + '\'' +
				", condType='" + condType + '\'' +
				'}';
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getConstraintName() {
		return constraintName;
	}

	public void setConstraintName(String constraintName) {
		this.constraintName = constraintName;
	}

	public String getConstraint() {
		return constraint;
	}

	public void setConstraint(String constraint) {
		this.constraint = constraint;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getCondStart() {
		return condStart;
	}

	public void setCondStart(String condStart) {
		this.condStart = condStart;
	}

	public String getCondEnd() {
		return condEnd;
	}

	public void setCondEnd(String condEnd) {
		this.condEnd = condEnd;
	}

}
