package com.roomsync.model;

public enum BillFrequency {
	WEEKLY("Weekly"),
	MONTHLY("Monthly");

	private final String label;

	BillFrequency(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
