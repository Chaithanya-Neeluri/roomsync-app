package com.roomsync.model;

public enum PaymentMethod {
	UPI("UPI"),
	CASH("Cash"),
	BANK_TRANSFER("Bank transfer"),
	OTHER("Other");

	private final String label;

	PaymentMethod(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
