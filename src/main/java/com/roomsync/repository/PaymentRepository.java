package com.roomsync.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.roomsync.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	List<Payment> findAllByOrderByPaymentDateDesc();

	boolean existsByRoommateId(Long roommateId);

	@Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p")
	BigDecimal sumAllPayments();

	@Query("""
			SELECT COALESCE(SUM(p.amount), 0)
			FROM Payment p
			WHERE p.roommate.id = :roommateId
			""")
	BigDecimal sumByRoommate(@Param("roommateId") Long roommateId);
}
