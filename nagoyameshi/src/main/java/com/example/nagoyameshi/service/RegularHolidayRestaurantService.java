package com.example.nagoyameshi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.RegularHoliday;
import com.example.nagoyameshi.entity.RegularHolidayRestaurant;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.RegularHolidayRepository;
import com.example.nagoyameshi.repository.RegularHolidayRestaurantRepository;

@Service
public class RegularHolidayRestaurantService {
	private final RegularHolidayRestaurantRepository regularHolidayRestaurantRepository;
	private final RegularHolidayRepository regularHolidayRepository;

	public RegularHolidayRestaurantService(RegularHolidayRestaurantRepository regularHolidayRestaurantRepository,
			RegularHolidayRepository regularHolidayRepository) {
		this.regularHolidayRestaurantRepository = regularHolidayRestaurantRepository;
		this.regularHolidayRepository = regularHolidayRepository;
	}

	@Transactional
	public void create(List<Integer> regularHolidayIds, Restaurant restaurant) {
		for (Integer regularHolidayId : regularHolidayIds) {
			RegularHolidayRestaurant regularHolidayRestaurant = new RegularHolidayRestaurant();
			RegularHoliday regularHoliday = regularHolidayRepository.getReferenceById(regularHolidayId);

			regularHolidayRestaurant.setRestaurant(restaurant);
			regularHolidayRestaurant.setRegularHoliday(regularHoliday);

			regularHolidayRestaurantRepository.save(regularHolidayRestaurant);
		}
	}

	@Transactional
	public void update(List<Integer> newRegularHolidayIds, Restaurant restaurant) {
		List<RegularHolidayRestaurant> existingRegularHolidayRestaurants = regularHolidayRestaurantRepository
				.findByRestaurantOrderByRegularHoliday_IdAsc(restaurant);
		List<Integer> existingRegularHolidayIds = regularHolidayRestaurantRepository
				.findRegularHolidayIdsByRestaurantOrderByRegularHoliday_IdAsc(restaurant);

		if (newRegularHolidayIds == null) {
			// newRegularHolidayIdsがnullの場合はすべてのエンティティを削除する
			for (RegularHolidayRestaurant existingRegularHolidayRestaurant : existingRegularHolidayRestaurants) {
				regularHolidayRestaurantRepository.delete(existingRegularHolidayRestaurant);
			}
		} else {
			// 既存のエンティティが新しいリストに存在しない場合は削除する
			for (RegularHolidayRestaurant existingRegularHolidayRestaurant : existingRegularHolidayRestaurants) {
				if (!newRegularHolidayIds.contains(existingRegularHolidayRestaurant.getRegularHoliday().getId())) {
					regularHolidayRestaurantRepository.delete(existingRegularHolidayRestaurant);
				}
			}

			// 新しいIDが既存のエンティティに存在しない場合は新たにエンティティを作成する
			for (Integer newRegularHolidayId : newRegularHolidayIds) {
				if (!existingRegularHolidayIds.contains(newRegularHolidayId)) {
					RegularHolidayRestaurant regularHolidayRestaurant = new RegularHolidayRestaurant();
					RegularHoliday regularHoliday = regularHolidayRepository.getReferenceById(newRegularHolidayId);

					regularHolidayRestaurant.setRestaurant(restaurant);
					regularHolidayRestaurant.setRegularHoliday(regularHoliday);

					regularHolidayRestaurantRepository.save(regularHolidayRestaurant);
				}
			}
		}
	}

	@Transactional
	public void deleteByRestaurant(Restaurant restaurant) {
		regularHolidayRestaurantRepository.deleteByRestaurant(restaurant);
	}

	// 店舗の定休日のday_indexの値をリストで返す
	public List<Integer> getRestaurantRegularHolidays(Restaurant restaurant) {
		return regularHolidayRestaurantRepository.findByRestaurantOrderByRegularHoliday_IdAsc(restaurant)
				.stream()
				.map(RegularHolidayRestaurant::getRegularHoliday)
				.map(RegularHoliday::getDayIndex)
				.collect(Collectors.toList());
	}
}
