package com.roomsync.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "expense_shares")
public class ExpenseShare {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "expense_id")
	private Expense expense;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "roommate_id")
	private Roommate roommate;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal allocatedAmount;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal paidAmount = BigDecimal.ZERO.setScale(2);

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PaymentStatus status = PaymentStatus.PENDING;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Expense getExpense() {
		return expense;
	}

	public void setExpense(Expense expense) {
		this.expense = expense;
	}

	public Roommate getRoommate() {
		return roommate;
	}

	public void setRoommate(Roommate roommate) {
		this.roommate = roommate;
	}

	public BigDecimal getAllocatedAmount() {
		return allocatedAmount;
	}

	public void setAllocatedAmount(BigDecimal allocatedAmount) {
		this.allocatedAmount = allocatedAmount;
	}

	public BigDecimal getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(BigDecimal paidAmount) {
		this.paidAmount = paidAmount;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
	}

	public BigDecimal getRemainingAmount() {
		BigDecimal remaining = allocatedAmount.subtract(paidAmount).setScale(2, RoundingMode.HALF_UP);
		return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO.setScale(2) : remaining;
	}

	public void refreshStatus() {
		if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
			status = PaymentStatus.PENDING;
		} else if (paidAmount.compareTo(allocatedAmount) >= 0) {
			status = PaymentStatus.PAID;
			paidAmount = allocatedAmount;
		} else {
			status = PaymentStatus.PARTIALLY_PAID;
		}
	}
}
