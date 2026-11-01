package com.roomsync.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roomsync.model.Expense;
import com.roomsync.model.ExpenseCategory;
import com.roomsync.model.ExpenseShare;
import com.roomsync.model.RecurringBill;
import com.roomsync.model.Roommate;
import com.roomsync.model.SplitMethod;
import com.roomsync.repository.ExpenseRepository;
import com.roomsync.repository.RoommateRepository;

@Service
public class ExpenseService {

	private final ExpenseRepository expenseRepository;
	private final RoommateRepository roommateRepository;
	private final BillSplitService billSplitService;

	public ExpenseService(ExpenseRepository expenseRepository, RoommateRepository roommateRepository,
			BillSplitService billSplitService) {
		this.expenseRepository = expenseRepository;
		this.roommateRepository = roommateRepository;
		this.billSplitService = billSplitService;
	}

	public List<Expense> findAll() {
		return expenseRepository.findAllByOrderByExpenseDateDesc();
	}

	public List<Expense> search(String query, ExpenseCategory category, LocalDate fromDate, LocalDate toDate) {
		String cleanedQuery = (query == null || query.isBlank()) ? null : query.trim();
		return expenseRepository.search(cleanedQuery, category, fromDate, toDate);
	}

	public Expense getById(Long id) {
		return expenseRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Expense not found."));
	}

	@Transactional
	public Expense create(String name, ExpenseCategory category, BigDecimal totalAmount, LocalDate expenseDate,
			LocalDate dueDate, Long paidById, SplitMethod splittingMethod, String description,
			Map<Long, BigDecimal> customAmounts, RecurringBill recurringBill) {
		List<Roommate> roommates = roommateRepository.findAllByOrderByNameAsc();
		Map<Roommate, BigDecimal> split = billSplitService.calculate(totalAmount, splittingMethod, roommates, customAmounts);
		Roommate paidBy = roommateRepository.findById(paidById)
				.orElseThrow(() -> new IllegalArgumentException("Select who paid this bill."));

		Expense expense = new Expense();
		expense.setName(name.trim());
		expense.setCategory(category);
		expense.setTotalAmount(Money.of(totalAmount));
		expense.setExpenseDate(expenseDate);
		expense.setDueDate(dueDate);
		expense.setPaidBy(paidBy);
		expense.setSplittingMethod(splittingMethod);
		expense.setDescription(blankToNull(description));
		expense.setRecurringBill(recurringBill);

		split.forEach((roommate, amount) -> {
			ExpenseShare share = new ExpenseShare();
			share.setRoommate(roommate);
			share.setAllocatedAmount(amount);
			if (roommate.getId().equals(paidBy.getId())) {
				share.setPaidAmount(amount);
			} else {
				share.setPaidAmount(Money.ZERO);
			}
			share.refreshStatus();
			expense.addShare(share);
		});
		return expenseRepository.save(expense);
	}

	@Transactional
	public Expense update(Long id, String name, ExpenseCategory category, BigDecimal totalAmount, LocalDate expenseDate,
			LocalDate dueDate, Long paidById, SplitMethod splittingMethod, String description,
			Map<Long, BigDecimal> customAmounts) {
		Expense expense = getById(id);
		boolean hasExtraPayments = expense.getShares().stream()
				.anyMatch(share -> !share.getRoommate().getId().equals(expense.getPaidBy().getId())
						&& share.getPaidAmount().compareTo(BigDecimal.ZERO) > 0);
		expense.setName(name.trim());
		expense.setCategory(category);
		expense.setDueDate(dueDate);
		expense.setDescription(blankToNull(description));
		if (hasExtraPayments) {
			return expenseRepository.save(expense);
		}

		List<Roommate> roommates = roommateRepository.findAllByOrderByNameAsc();
		Map<Roommate, BigDecimal> split = billSplitService.calculate(totalAmount, splittingMethod, roommates,
				customAmounts);
		Roommate paidBy = roommateRepository.findById(paidById)
				.orElseThrow(() -> new IllegalArgumentException("Select who paid this bill."));
		expense.setTotalAmount(Money.of(totalAmount));
		expense.setExpenseDate(expenseDate);
		expense.setPaidBy(paidBy);
		expense.setSplittingMethod(splittingMethod);
		expense.getShares().clear();
		split.forEach((roommate, amount) -> {
			ExpenseShare share = new ExpenseShare();
			share.setRoommate(roommate);
			share.setAllocatedAmount(amount);
			share.setPaidAmount(roommate.getId().equals(paidBy.getId()) ? amount : Money.ZERO);
			share.refreshStatus();
			expense.addShare(share);
		});
		return expenseRepository.save(expense);
	}

	@Transactional
	public void delete(Long id) {
		expenseRepository.deleteById(id);
	}

	public List<Expense> recent() {
		return expenseRepository.findTop6ByOrderByExpenseDateDesc();
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
