package com.roomsync.model;

public enum ExpenseCategory {
	RENT("Rent"),
	ELECTRICITY("Electricity"),
	WATER("Water"),
	INTERNET("Internet"),
	GROCERIES("Groceries"),
	GAS("Gas"),
	MAINTENANCE("Maintenance"),
	OTHER("Other");

	private final String label;

	ExpenseCategory(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
