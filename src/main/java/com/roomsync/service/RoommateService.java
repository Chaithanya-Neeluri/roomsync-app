package com.roomsync.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roomsync.model.Roommate;
import com.roomsync.repository.ExpenseRepository;
import com.roomsync.repository.ExpenseShareRepository;
import com.roomsync.repository.PaymentRepository;
import com.roomsync.repository.RecurringBillRepository;
import com.roomsync.repository.RoommateRepository;

@Service
public class RoommateService {

	private final RoommateRepository roommateRepository;
	private final ExpenseRepository expenseRepository;
	private final ExpenseShareRepository expenseShareRepository;
	private final PaymentRepository paymentRepository;
	private final RecurringBillRepository recurringBillRepository;

	public RoommateService(RoommateRepository roommateRepository, ExpenseRepository expenseRepository,
			ExpenseShareRepository expenseShareRepository, PaymentRepository paymentRepository,
			RecurringBillRepository recurringBillRepository) {
		this.roommateRepository = roommateRepository;
		this.expenseRepository = expenseRepository;
		this.expenseShareRepository = expenseShareRepository;
		this.paymentRepository = paymentRepository;
		this.recurringBillRepository = recurringBillRepository;
	}

	public List<Roommate> findAll() {
		return roommateRepository.findAllByOrderByNameAsc();
	}

	public Roommate getById(Long id) {
		return roommateRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Roommate not found."));
	}

	@Transactional
	public Roommate save(Roommate roommate) {
		if (roommate.getName() == null || roommate.getName().isBlank()) {
			throw new IllegalArgumentException("Name is required.");
		}
		roommate.setName(roommate.getName().trim());
		if (roommate.getPhone() != null && roommate.getPhone().isBlank()) {
			roommate.setPhone(null);
		}
		if (roommate.getId() == null) {
			roommate.setDateAdded(LocalDate.now());
		} else {
			Roommate existing = getById(roommate.getId());
			roommate.setDateAdded(existing.getDateAdded());
		}
		return roommateRepository.save(roommate);
	}

	@Transactional
	public void delete(Long id) {
		if (expenseRepository.existsByPaidById(id) || expenseShareRepository.existsByRoommateId(id)
				|| paymentRepository.existsByRoommateId(id) || recurringBillRepository.existsByPaidById(id)) {
			throw new IllegalArgumentException(
					"This roommate is linked to expenses, payments or bills and cannot be deleted.");
		}
		roommateRepository.deleteById(id);
	}

	public long count() {
		return roommateRepository.count();
	}
}
