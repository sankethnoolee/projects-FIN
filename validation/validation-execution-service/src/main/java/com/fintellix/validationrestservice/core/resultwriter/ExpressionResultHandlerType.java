package com.fintellix.validationrestservice.core.resultwriter;

public enum ExpressionResultHandlerType {
	CSV("csv"), MULTICSV("multicsv"), MONGO("mongo");

	private final String value;

	ExpressionResultHandlerType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ExpressionResultHandlerType getName(String val) {
		for (ExpressionResultHandlerType type : ExpressionResultHandlerType.values()) {
			if (type.value.equalsIgnoreCase(val)) {
				return type;
			}
		}

		throw new IllegalArgumentException("Not a valid value : " + val);
	}
}
