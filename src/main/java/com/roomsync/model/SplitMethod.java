package com.roomsync.model;

public enum SplitMethod {
	EQUAL("Equal split"),
	CUSTOM("Custom split");

	private final String label;

	SplitMethod(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
