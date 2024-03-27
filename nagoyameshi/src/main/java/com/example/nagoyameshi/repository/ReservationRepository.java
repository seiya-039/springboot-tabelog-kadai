package com.example.nagoyameshi.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
	public Page<Reservation> findByUserOrderByReservedDatetimeDesc(User user, Pageable pageable);

	public List<Reservation> findByUserAndReservedDatetimeBetween(User user, LocalDateTime start, LocalDateTime end);

	public List<Reservation> findByRestaurantAndReservedDatetimeBetween(Restaurant restaurant, LocalDateTime start,
			LocalDateTime end);

	public void deleteByRestaurant(Restaurant restaurant);
}
