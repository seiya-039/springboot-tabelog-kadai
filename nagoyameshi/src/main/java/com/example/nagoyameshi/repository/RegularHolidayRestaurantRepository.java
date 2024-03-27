package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.nagoyameshi.entity.RegularHolidayRestaurant;
import com.example.nagoyameshi.entity.Restaurant;

public interface RegularHolidayRestaurantRepository extends JpaRepository<RegularHolidayRestaurant, Integer> {
	public List<RegularHolidayRestaurant> findByRestaurantOrderByRegularHoliday_IdAsc(Restaurant restaurant);

	@Query("SELECT rhr.regularHoliday.id FROM RegularHolidayRestaurant rhr WHERE rhr.restaurant = :restaurant ORDER BY rhr.id ASC")
	public List<Integer> findRegularHolidayIdsByRestaurantOrderByRegularHoliday_IdAsc(
			@Param("restaurant") Restaurant restaurant);

	public void deleteByRestaurant(Restaurant restaurant);
}
