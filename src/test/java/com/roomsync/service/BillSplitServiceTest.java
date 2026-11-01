package com.roomsync.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.roomsync.model.Roommate;
import com.roomsync.model.SplitMethod;

class BillSplitServiceTest {

	private final BillSplitService service = new BillSplitService();

	@Test
	void equalSplitDividesThreeThousandAmongThreePeople() {
		Map<Roommate, BigDecimal> result = service.calculate(new BigDecimal("3000.00"), SplitMethod.EQUAL,
				List.of(roommate(1L, "A"), roommate(2L, "B"), roommate(3L, "C")), Map.of());
		assertThat(result.values()).containsExactly(
				new BigDecimal("1000.00"),
				new BigDecimal("1000.00"),
				new BigDecimal("1000.00"));
	}

	@Test
	void customSplitRequiresExactTotal() {
		List<Roommate> roommates = List.of(roommate(1L, "A"), roommate(2L, "B"), roommate(3L, "C"));
		Map<Roommate, BigDecimal> result = service.calculate(new BigDecimal("3000.00"), SplitMethod.CUSTOM, roommates,
				Map.of(1L, new BigDecimal("1500"), 2L, new BigDecimal("1000"), 3L, new BigDecimal("500")));
		assertThat(result.get(roommates.get(0))).isEqualByComparingTo("1500.00");
		assertThat(result.get(roommates.get(1))).isEqualByComparingTo("1000.00");
		assertThat(result.get(roommates.get(2))).isEqualByComparingTo("500.00");
	}

	@Test
	void customSplitRejectsMismatchedTotal() {
		assertThatThrownBy(() -> service.calculate(new BigDecimal("3000.00"), SplitMethod.CUSTOM,
				List.of(roommate(1L, "A"), roommate(2L, "B")),
				Map.of(1L, new BigDecimal("1000"), 2L, new BigDecimal("1000"))))
				.isInstanceOf(IllegalArgumentException.class);
	}

	private Roommate roommate(Long id, String name) {
		Roommate roommate = new Roommate();
		roommate.setId(id);
		roommate.setName(name);
		return roommate;
	}
}
