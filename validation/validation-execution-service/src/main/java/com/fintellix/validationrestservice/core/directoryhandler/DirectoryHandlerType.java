package com.fintellix.validationrestservice.core.directoryhandler;

public enum DirectoryHandlerType {
	DEFAULT("default");

	private final String value;

	DirectoryHandlerType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static DirectoryHandlerType getName(String val) {
		for (DirectoryHandlerType type : DirectoryHandlerType.values()) {
			if (type.value.equalsIgnoreCase(val)) {
				return type;
			}
		}

		throw new IllegalArgumentException("Not a valid value : " + val);
	}
}
