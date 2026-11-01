package com.roomsync.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.roomsync.model.Expense;
import com.roomsync.model.ExpenseCategory;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

	List<Expense> findAllByOrderByExpenseDateDesc();

	List<Expense> findTop6ByOrderByExpenseDateDesc();

	boolean existsByRecurringBillIdAndExpenseDate(Long recurringBillId, LocalDate expenseDate);

	@org.springframework.data.jpa.repository.Modifying
	@Query("UPDATE Expense e SET e.recurringBill = null WHERE e.recurringBill.id = :id")
	void clearRecurringBill(@Param("id") Long id);

	boolean existsByPaidById(Long roommateId);

	@Query("""
			SELECT COALESCE(SUM(e.totalAmount), 0)
			FROM Expense e
			""")
	BigDecimal sumTotalAmount();

	@Query("""
			SELECT COALESCE(SUM(e.totalAmount), 0)
			FROM Expense e
			WHERE e.paidBy.id = :roommateId
			""")
	BigDecimal sumPaidByRoommate(@Param("roommateId") Long roommateId);

	@Query("""
			SELECT e FROM Expense e
			WHERE (:query IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')))
			  AND (:category IS NULL OR e.category = :category)
			  AND (:fromDate IS NULL OR e.expenseDate >= :fromDate)
			  AND (:toDate IS NULL OR e.expenseDate <= :toDate)
			ORDER BY e.expenseDate DESC
			""")
	List<Expense> search(
			@Param("query") String query,
			@Param("category") ExpenseCategory category,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);

	@Query("""
			SELECT FUNCTION('DATE_FORMAT', e.expenseDate, '%Y-%m') AS monthKey, COALESCE(SUM(e.totalAmount), 0)
			FROM Expense e
			WHERE e.expenseDate >= :fromDate
			GROUP BY FUNCTION('DATE_FORMAT', e.expenseDate, '%Y-%m')
			ORDER BY monthKey
			""")
	List<Object[]> sumByMonth(@Param("fromDate") LocalDate fromDate);
}
