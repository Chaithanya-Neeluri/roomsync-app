package com.roomsync.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.roomsync.model.Roommate;
import com.roomsync.model.SplitMethod;

@Service
public class BillSplitService {

	public Map<Roommate, BigDecimal> calculate(BigDecimal totalAmount, SplitMethod method, List<Roommate> roommates,
			Map<Long, BigDecimal> customAmounts) {
		if (roommates == null || roommates.isEmpty()) {
			throw new IllegalArgumentException("Add at least one roommate before splitting a bill.");
		}
		BigDecimal total = Money.of(totalAmount);
		if (total.compareTo(Money.ZERO) <= 0) {
			throw new IllegalArgumentException("Expense amount must be greater than 0.");
		}
		if (method == SplitMethod.CUSTOM) {
			return customSplit(total, roommates, customAmounts);
		}
		return equalSplit(total, roommates);
	}

	private Map<Roommate, BigDecimal> equalSplit(BigDecimal total, List<Roommate> roommates) {
		int count = roommates.size();
		BigDecimal share = total.divide(BigDecimal.valueOf(count), Money.SCALE, RoundingMode.DOWN);
		BigDecimal remainder = total.subtract(share.multiply(BigDecimal.valueOf(count)));

		Map<Roommate, BigDecimal> result = new LinkedHashMap<>();
		for (int i = 0; i < count; i++) {
			BigDecimal amount = i == 0 ? share.add(remainder) : share;
			result.put(roommates.get(i), Money.of(amount));
		}
		assertTotalsMatch(total, result);
		return result;
	}

	private Map<Roommate, BigDecimal> customSplit(BigDecimal total, List<Roommate> roommates,
			Map<Long, BigDecimal> customAmounts) {
		if (customAmounts == null || customAmounts.isEmpty()) {
			throw new IllegalArgumentException("Enter a custom amount for each roommate.");
		}
		Map<Roommate, BigDecimal> result = new LinkedHashMap<>();
		BigDecimal sum = Money.ZERO;
		for (Roommate roommate : roommates) {
			BigDecimal amount = Money.of(customAmounts.get(roommate.getId()));
			if (amount.compareTo(Money.ZERO) < 0) {
				throw new IllegalArgumentException("Custom amounts cannot be negative.");
			}
			sum = sum.add(amount);
			result.put(roommate, amount);
		}
		if (sum.compareTo(total) != 0) {
			throw new IllegalArgumentException(
					"Custom amounts must add up to ₹" + total + ". Current total is ₹" + sum + ".");
		}
		return result;
	}

	private void assertTotalsMatch(BigDecimal total, Map<Roommate, BigDecimal> result) {
		BigDecimal sum = result.values().stream().reduce(Money.ZERO, BigDecimal::add);
		if (sum.compareTo(total) != 0) {
			throw new IllegalArgumentException("Split total does not match the expense amount.");
		}
	}

	public List<SplitPreview> preview(BigDecimal totalAmount, SplitMethod method, List<Roommate> roommates,
			Map<Long, BigDecimal> customAmounts) {
		Map<Roommate, BigDecimal> calculated = calculate(totalAmount, method, roommates, customAmounts);
		List<SplitPreview> preview = new ArrayList<>();
		calculated.forEach((roommate, amount) -> preview.add(new SplitPreview(roommate.getId(), roommate.getName(), amount)));
		return preview;
	}

	public record SplitPreview(Long roommateId, String roommateName, BigDecimal amount) {
	}
}
