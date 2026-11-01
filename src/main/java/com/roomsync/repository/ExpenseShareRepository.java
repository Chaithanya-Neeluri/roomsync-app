package com.roomsync.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.roomsync.model.ExpenseShare;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {

	Optional<ExpenseShare> findByExpenseIdAndRoommateId(Long expenseId, Long roommateId);

	boolean existsByRoommateId(Long roommateId);

	@Query("SELECT COALESCE(SUM(s.paidAmount), 0) FROM ExpenseShare s")
	BigDecimal sumPaidAmount();

	@Query("""
			SELECT COALESCE(SUM(s.allocatedAmount - s.paidAmount), 0)
			FROM ExpenseShare s
			WHERE s.paidAmount < s.allocatedAmount
			""")
	BigDecimal sumPendingAmount();

	@Query("""
			SELECT COALESCE(SUM(s.allocatedAmount), 0)
			FROM ExpenseShare s
			WHERE s.roommate.id = :roommateId
			""")
	BigDecimal sumAllocatedByRoommate(@Param("roommateId") Long roommateId);

	@Query("""
			SELECT COALESCE(SUM(s.paidAmount), 0)
			FROM ExpenseShare s
			WHERE s.roommate.id = :roommateId
			""")
	BigDecimal sumSettledByRoommate(@Param("roommateId") Long roommateId);

	@Query("""
			SELECT s FROM ExpenseShare s
			JOIN FETCH s.expense e
			JOIN FETCH s.roommate r
			JOIN FETCH e.paidBy
			WHERE s.status <> com.roomsync.model.PaymentStatus.PAID
			ORDER BY e.dueDate ASC, e.expenseDate DESC
			""")
	List<ExpenseShare> findPendingShares();
}
