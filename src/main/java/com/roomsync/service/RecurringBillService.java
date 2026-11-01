package com.roomsync.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roomsync.model.BillFrequency;
import com.roomsync.model.ExpenseCategory;
import com.roomsync.model.RecurringBill;
import com.roomsync.model.Roommate;
import com.roomsync.model.SplitMethod;
import com.roomsync.repository.ExpenseRepository;
import com.roomsync.repository.RecurringBillRepository;
import com.roomsync.repository.RoommateRepository;

@Service
public class RecurringBillService implements ApplicationRunner {

	private final RecurringBillRepository recurringBillRepository;
	private final RoommateRepository roommateRepository;
	private final ExpenseRepository expenseRepository;
	private final ExpenseService expenseService;
	private final BillSplitService billSplitService;

	public RecurringBillService(RecurringBillRepository recurringBillRepository,
			RoommateRepository roommateRepository, ExpenseRepository expenseRepository, ExpenseService expenseService,
			BillSplitService billSplitService) {
		this.recurringBillRepository = recurringBillRepository;
		this.roommateRepository = roommateRepository;
		this.expenseRepository = expenseRepository;
		this.expenseService = expenseService;
		this.billSplitService = billSplitService;
	}

	public List<RecurringBill> findAll() {
		return recurringBillRepository.findAllByOrderByNextDueDateAsc();
	}

	public RecurringBill getById(Long id) {
		return recurringBillRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Recurring bill not found."));
	}

	@Transactional
	public RecurringBill save(Long id, String billName, ExpenseCategory category, BigDecimal amount,
			BillFrequency frequency, LocalDate startDate, LocalDate nextDueDate, SplitMethod splittingMethod,
			Long paidById, boolean active, Map<Long, BigDecimal> customAmounts) {
		List<Roommate> roommates = roommateRepository.findAllByOrderByNameAsc();
		billSplitService.calculate(amount, splittingMethod, roommates, customAmounts);

		RecurringBill bill = id == null ? new RecurringBill() : getById(id);
		bill.setBillName(billName.trim());
		bill.setCategory(category);
		bill.setAmount(Money.of(amount));
		bill.setFrequency(frequency);
		bill.setStartDate(startDate);
		bill.setNextDueDate(nextDueDate != null ? nextDueDate : startDate);
		bill.setSplittingMethod(splittingMethod);
		bill.setActive(active);
		bill.setPaidBy(roommateRepository.findById(paidById)
				.orElseThrow(() -> new IllegalArgumentException("Select who usually pays this bill.")));
		bill.getCustomShares().clear();
		if (splittingMethod == SplitMethod.CUSTOM && customAmounts != null) {
			bill.getCustomShares().putAll(customAmounts);
		}
		return recurringBillRepository.save(bill);
	}

	@Transactional
	public void toggleActive(Long id) {
		RecurringBill bill = getById(id);
		bill.setActive(!bill.isActive());
		recurringBillRepository.save(bill);
	}

	@Transactional
	public void delete(Long id) {
		expenseRepository.clearRecurringBill(id);
		recurringBillRepository.deleteById(id);
	}

	public List<RecurringBill> upcoming() {
		return recurringBillRepository.findTop5ByActiveTrueOrderByNextDueDateAsc();
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		generateDueBills();
	}

	@Scheduled(cron = "0 0 1 * * *")
	@Transactional
	public void generateDueBills() {
		LocalDate today = LocalDate.now();
		List<RecurringBill> dueBills = recurringBillRepository.findByActiveTrueAndNextDueDateLessThanEqual(today);
		for (RecurringBill bill : dueBills) {
			while (bill.isActive() && !bill.getNextDueDate().isAfter(today)) {
				LocalDate dueDate = bill.getNextDueDate();
				if (!expenseRepository.existsByRecurringBillIdAndExpenseDate(bill.getId(), dueDate)) {
					Map<Long, BigDecimal> custom = bill.getSplittingMethod() == SplitMethod.CUSTOM
							? bill.getCustomShares()
							: Map.of();
					expenseService.create(
							bill.getBillName(),
							bill.getCategory(),
							bill.getAmount(),
							dueDate,
							dueDate,
							bill.getPaidBy().getId(),
							bill.getSplittingMethod(),
							"Generated from recurring bill",
							custom,
							bill);
				}
				bill.setNextDueDate(nextDate(dueDate, bill.getFrequency()));
			}
			recurringBillRepository.save(bill);
		}
	}

	private LocalDate nextDate(LocalDate current, BillFrequency frequency) {
		if (frequency == BillFrequency.WEEKLY) {
			return current.plusWeeks(1);
		}
		return current.plusMonths(1);
	}
}
