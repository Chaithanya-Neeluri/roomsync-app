package com.roomsync.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roomsync.model.ExpenseShare;
import com.roomsync.model.Payment;
import com.roomsync.model.PaymentMethod;
import com.roomsync.repository.ExpenseShareRepository;
import com.roomsync.repository.PaymentRepository;

@Service
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final ExpenseShareRepository expenseShareRepository;

	public PaymentService(PaymentRepository paymentRepository, ExpenseShareRepository expenseShareRepository) {
		this.paymentRepository = paymentRepository;
		this.expenseShareRepository = expenseShareRepository;
	}

	public List<Payment> findAll() {
		return paymentRepository.findAllByOrderByPaymentDateDesc();
	}

	public List<ExpenseShare> findPendingShares() {
		return expenseShareRepository.findPendingShares();
	}

	@Transactional
	public Payment record(Long expenseId, Long roommateId, BigDecimal amount, LocalDate paymentDate,
			PaymentMethod method, String notes) {
		ExpenseShare share = expenseShareRepository.findByExpenseIdAndRoommateId(expenseId, roommateId)
				.orElseThrow(() -> new IllegalArgumentException("No share found for this roommate and expense."));
		BigDecimal paymentAmount = Money.of(amount);
		if (paymentAmount.compareTo(Money.ZERO) <= 0) {
			throw new IllegalArgumentException("Payment amount must be greater than 0.");
		}
		if (paymentAmount.compareTo(share.getRemainingAmount()) > 0) {
			throw new IllegalArgumentException("Payment cannot exceed the remaining amount of ₹"
					+ share.getRemainingAmount() + ".");
		}

		Payment payment = new Payment();
		payment.setExpense(share.getExpense());
		payment.setRoommate(share.getRoommate());
		payment.setAmount(paymentAmount);
		payment.setPaymentDate(paymentDate != null ? paymentDate : LocalDate.now());
		payment.setPaymentMethod(method);
		payment.setNotes(notes == null || notes.isBlank() ? null : notes.trim());
		paymentRepository.save(payment);

		share.setPaidAmount(share.getPaidAmount().add(paymentAmount));
		share.refreshStatus();
		expenseShareRepository.save(share);
		return payment;
	}
}
