package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.nagoyameshi.entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {
	public Page<Restaurant> findByNameLike(String keyword, Pageable pageable);

	public Page<Restaurant> findAllByOrderByCreatedAtDesc(Pageable pageable);

	@Query(value = "SELECT r.* FROM restaurants r LEFT JOIN category_restaurant cr ON r.id = cr.restaurant_id LEFT JOIN categories c ON cr.category_id = c.id WHERE r.name LIKE :keyword OR r.address LIKE :keyword OR c.name LIKE :keyword GROUP BY r.id", countQuery = "SELECT count(*) FROM (SELECT r.* FROM restaurants r LEFT JOIN category_restaurant cr ON r.id = cr.restaurant_id LEFT JOIN categories c ON cr.category_id = c.id WHERE r.name LIKE :keyword OR r.address LIKE :keyword OR c.name LIKE :keyword GROUP BY r.id) AS countQuery", nativeQuery = true)
	List<Restaurant> findByNameLikeOrAddressLikeOrCategoryNameLike(String keyword);

	@Query("SELECT r FROM Restaurant r JOIN r.categoryRestaurants cr WHERE cr.category.id = :categoryId")
	List<Restaurant> findByCategoryId(@Param("categoryId") Integer categoryId);

	List<Restaurant> findByLowestPriceLessThanEqual(Integer price);
}
