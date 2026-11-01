package com.roomsync.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

	public static final int SCALE = 2;
	public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
	public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE);

	private Money() {
	}

	public static BigDecimal of(BigDecimal value) {
		if (value == null) {
			return ZERO;
		}
		return value.setScale(SCALE, ROUNDING);
	}
}
