package com.roomsync.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.roomsync.model.RecurringBill;

public interface RecurringBillRepository extends JpaRepository<RecurringBill, Long> {

	List<RecurringBill> findAllByOrderByNextDueDateAsc();

	List<RecurringBill> findByActiveTrueAndNextDueDateLessThanEqual(LocalDate date);

	List<RecurringBill> findTop5ByActiveTrueOrderByNextDueDateAsc();

	boolean existsByPaidById(Long roommateId);
}
