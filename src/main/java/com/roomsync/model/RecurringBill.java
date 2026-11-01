package com.roomsync.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "recurring_bills")
public class RecurringBill {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String billName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ExpenseCategory category;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BillFrequency frequency;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate nextDueDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SplitMethod splittingMethod;

	@Column(nullable = false)
	private boolean active = true;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "paid_by_id")
	private Roommate paidBy;

	@ElementCollection
	@CollectionTable(name = "recurring_custom_shares", joinColumns = @JoinColumn(name = "recurring_bill_id"))
	@MapKeyColumn(name = "roommate_id")
	@Column(name = "share_amount", precision = 12, scale = 2)
	private Map<Long, BigDecimal> customShares = new HashMap<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBillName() {
		return billName;
	}

	public void setBillName(String billName) {
		this.billName = billName;
	}

	public ExpenseCategory getCategory() {
		return category;
	}

	public void setCategory(ExpenseCategory category) {
		this.category = category;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BillFrequency getFrequency() {
		return frequency;
	}

	public void setFrequency(BillFrequency frequency) {
		this.frequency = frequency;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getNextDueDate() {
		return nextDueDate;
	}

	public void setNextDueDate(LocalDate nextDueDate) {
		this.nextDueDate = nextDueDate;
	}

	public SplitMethod getSplittingMethod() {
		return splittingMethod;
	}

	public void setSplittingMethod(SplitMethod splittingMethod) {
		this.splittingMethod = splittingMethod;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Roommate getPaidBy() {
		return paidBy;
	}

	public void setPaidBy(Roommate paidBy) {
		this.paidBy = paidBy;
	}

	public Map<Long, BigDecimal> getCustomShares() {
		return customShares;
	}

	public void setCustomShares(Map<Long, BigDecimal> customShares) {
		this.customShares = customShares;
	}
}
