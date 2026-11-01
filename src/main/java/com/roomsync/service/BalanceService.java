package com.roomsync.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.roomsync.model.Roommate;
import com.roomsync.repository.ExpenseRepository;
import com.roomsync.repository.ExpenseShareRepository;
import com.roomsync.repository.PaymentRepository;

@Service
public class BalanceService {

	private final RoommateService roommateService;
	private final ExpenseRepository expenseRepository;
	private final ExpenseShareRepository expenseShareRepository;
	private final PaymentRepository paymentRepository;

	public BalanceService(RoommateService roommateService, ExpenseRepository expenseRepository,
			ExpenseShareRepository expenseShareRepository, PaymentRepository paymentRepository) {
		this.roommateService = roommateService;
		this.expenseRepository = expenseRepository;
		this.expenseShareRepository = expenseShareRepository;
		this.paymentRepository = paymentRepository;
	}

	public List<RoommateBalance> calculateAll() {
		List<RoommateBalance> balances = new ArrayList<>();
		for (Roommate roommate : roommateService.findAll()) {
			BigDecimal totalPaid = Money.of(expenseRepository.sumPaidByRoommate(roommate.getId()));
			BigDecimal totalShare = Money.of(expenseShareRepository.sumAllocatedByRoommate(roommate.getId()));
			BigDecimal settled = Money.of(expenseShareRepository.sumSettledByRoommate(roommate.getId()));
			BigDecimal remainingOwed = totalShare.subtract(settled);
			if (remainingOwed.compareTo(Money.ZERO) < 0) {
				remainingOwed = Money.ZERO;
			}
			BigDecimal net = totalPaid.subtract(totalShare);
			balances.add(new RoommateBalance(roommate, totalPaid, totalShare, settled, remainingOwed, net));
		}
		return balances;
	}

	public record RoommateBalance(
			Roommate roommate,
			BigDecimal totalPaid,
			BigDecimal totalShare,
			BigDecimal settled,
			BigDecimal remainingOwed,
			BigDecimal net) {

		public boolean isReceivable() {
			return net.compareTo(BigDecimal.ZERO) > 0;
		}

		public boolean isPayable() {
			return net.compareTo(BigDecimal.ZERO) < 0;
		}

		public BigDecimal getAbsoluteNet() {
			return net.abs();
		}
	}
}
