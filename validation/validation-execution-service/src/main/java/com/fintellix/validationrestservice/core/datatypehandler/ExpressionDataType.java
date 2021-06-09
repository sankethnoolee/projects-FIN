package com.fintellix.validationrestservice.core.datatypehandler;

public enum ExpressionDataType {
	STRING("string"), BIGDECIMAL("bigdecimal"), DATE("date"), INTEGER("integer"), LONG("long")
	, DOUBLE("double"), BOOLEAN("boolean");

	private final String value;

	ExpressionDataType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ExpressionDataType getName(String val) {
		for (ExpressionDataType type : ExpressionDataType.values()) {
			if (type.value.equalsIgnoreCase(val)) {
				return type;
			}
		}

		throw new IllegalArgumentException("Not a valid value : " + val);
	}
}
