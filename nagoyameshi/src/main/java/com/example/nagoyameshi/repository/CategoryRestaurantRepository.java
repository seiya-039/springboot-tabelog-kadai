package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.CategoryRestaurant;
import com.example.nagoyameshi.entity.Restaurant;

public interface CategoryRestaurantRepository extends JpaRepository<CategoryRestaurant, Integer> {
	public List<CategoryRestaurant> findByRestaurantOrderByIdAsc(Restaurant restaurant);

	@Query("SELECT cr.category.id FROM CategoryRestaurant cr WHERE cr.restaurant = :restaurant ORDER BY cr.id ASC")
	public List<Integer> findCategoryIdsByRestaurantOrderByIdAsc(@Param("restaurant") Restaurant restaurant);

	public void deleteByRestaurant(Restaurant restaurant);

	public void deleteByCategory(Category category);
}