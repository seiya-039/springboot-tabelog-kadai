package com.example.nagoyameshi.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.example.nagoyameshi.repository.ReservationRepository;

@Service
public class ReservationService {
	private final ReservationRepository reservationRepository;

	public ReservationService(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	@Transactional
	public void create(Restaurant restaurant, User user, ReservationRegisterForm reservationRegisterForm) {
		Reservation reservation = new Reservation();
		LocalDateTime reservedDatetime = LocalDateTime.of(reservationRegisterForm.getReservationDate(),
				reservationRegisterForm.getReservationTime());

		reservation.setReservedDatetime(reservedDatetime);
		reservation.setNumberOfPeople(reservationRegisterForm.getNumberOfPeople());
		reservation.setRestaurant(restaurant);
		reservation.setUser(user);

		reservationRepository.save(reservation);
	}

	@Transactional
	public void delete(Reservation reservation) {
		reservationRepository.delete(reservation);
	}

	@Transactional
	public void deleteByRestaurant(Restaurant restaurant) {
		reservationRepository.deleteByRestaurant(restaurant);
	}

	// 予約日時が現在よりも2時間以上後であればtrueを返す
	public boolean isAtLeastTwoHoursInFuture(LocalDateTime reservationDateTime) {
		return Duration.between(LocalDateTime.now(), reservationDateTime).toHours() >= 2;
	}

	// 予約日時の2時間前後の予約がなければtrueを返す
	public boolean isTwoHoursApartFromOthers(LocalDateTime reservationDateTime, User user) {
		LocalDateTime twoHoursBefore = reservationDateTime.minusHours(2);
		LocalDateTime twoHoursAfter = reservationDateTime.plusHours(2);

		List<Reservation> reservations = reservationRepository.findByUserAndReservedDatetimeBetween(user,
				twoHoursBefore, twoHoursAfter);

		return reservations.isEmpty();
	}

	// 予約日時の2時間前後の予約の総人数を計算し、残りの座席数を取得する
	public int getRemainingSeats(LocalDateTime reservationDateTime, Restaurant restaurant) {
		LocalDateTime twoHoursBefore = reservationDateTime.minusHours(2);
		LocalDateTime twoHoursAfter = reservationDateTime.plusHours(2);

		List<Reservation> reservations = reservationRepository.findByRestaurantAndReservedDatetimeBetween(restaurant,
				twoHoursBefore, twoHoursAfter);

		Integer totalPeople = reservations.stream().mapToInt(Reservation::getNumberOfPeople).sum();

		return restaurant.getSeatingCapacity() - totalPeople;
	}
}
