package com.roomsync.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.roomsync.model.Expense;
import com.roomsync.model.RecurringBill;
import com.roomsync.repository.ExpenseRepository;
import com.roomsync.repository.ExpenseShareRepository;

@Service
public class DashboardService {

	private final ExpenseRepository expenseRepository;
	private final ExpenseShareRepository expenseShareRepository;
	private final RoommateService roommateService;
	private final RecurringBillService recurringBillService;
	private final BalanceService balanceService;

	public DashboardService(ExpenseRepository expenseRepository, ExpenseShareRepository expenseShareRepository,
			RoommateService roommateService, RecurringBillService recurringBillService,
			BalanceService balanceService) {
		this.expenseRepository = expenseRepository;
		this.expenseShareRepository = expenseShareRepository;
		this.roommateService = roommateService;
		this.recurringBillService = recurringBillService;
		this.balanceService = balanceService;
	}

	public DashboardView build() {
		BigDecimal totalExpenses = Money.of(expenseRepository.sumTotalAmount());
		BigDecimal totalPaid = Money.of(expenseShareRepository.sumPaidAmount());
		BigDecimal pending = Money.of(expenseShareRepository.sumPendingAmount());
		List<String> labels = new ArrayList<>();
		List<BigDecimal> totals = new ArrayList<>();
		Map<String, BigDecimal> byMonth = new HashMap<>();
		LocalDate from = LocalDate.now().minusMonths(5).withDayOfMonth(1);
		for (Object[] row : expenseRepository.sumByMonth(from)) {
			byMonth.put(String.valueOf(row[0]), Money.of((BigDecimal) row[1]));
		}
		YearMonth cursor = YearMonth.from(from);
		for (int i = 0; i < 6; i++) {
			String key = cursor.toString();
			labels.add(cursor.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
			totals.add(byMonth.getOrDefault(key, Money.ZERO));
			cursor = cursor.plusMonths(1);
		}
		return new DashboardView(
				totalExpenses,
				totalPaid,
				pending,
				roommateService.count(),
				labels,
				totals,
				expenseRepository.findTop6ByOrderByExpenseDateDesc(),
				recurringBillService.upcoming(),
				balanceService.calculateAll());
	}

	public record DashboardView(
			BigDecimal totalExpenses,
			BigDecimal totalPaid,
			BigDecimal pendingAmount,
			long roommateCount,
			List<String> chartLabels,
			List<BigDecimal> chartTotals,
			List<Expense> recentExpenses,
			List<RecurringBill> upcomingBills,
			List<BalanceService.RoommateBalance> contributions) {
	}
}
