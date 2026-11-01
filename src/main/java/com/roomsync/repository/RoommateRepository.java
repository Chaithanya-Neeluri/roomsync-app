package com.roomsync.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.roomsync.model.Roommate;

public interface RoommateRepository extends JpaRepository<Roommate, Long> {

	List<Roommate> findAllByOrderByNameAsc();
}
