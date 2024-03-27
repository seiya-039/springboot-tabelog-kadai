package com.example.nagoyameshi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReviewEditForm;
import com.example.nagoyameshi.form.ReviewRegisterForm;
import com.example.nagoyameshi.repository.ReviewRepository;

@Service
public class ReviewService {
	private final ReviewRepository reviewRepository;

	public ReviewService(ReviewRepository reviewRepository) {
		this.reviewRepository = reviewRepository;
	}

	@Transactional
	public void create(Restaurant restaurant, User user, ReviewRegisterForm reviewRegisterForm) {
		Review review = new Review();

		review.setContent(reviewRegisterForm.getContent());
		review.setScore(reviewRegisterForm.getScore());
		review.setRestaurant(restaurant);
		review.setUser(user);

		reviewRepository.save(review);
	}

	@Transactional
	public void update(ReviewEditForm reviewEditForm) {
		Review review = reviewRepository.getReferenceById(reviewEditForm.getId());

		review.setContent(reviewEditForm.getContent());
		review.setScore(reviewEditForm.getScore());

		reviewRepository.save(review);
	}

	@Transactional
	public void delete(Review review) {
		reviewRepository.delete(review);
	}

	@Transactional
	public void deleteByRestaurant(Restaurant restaurant) {
		reviewRepository.deleteByRestaurant(restaurant);
	}

	public boolean hasUserAlreadyReviewed(Restaurant restaurant, User user) {
		return reviewRepository.findByRestaurantAndUser(restaurant, user) != null;
	}
}
