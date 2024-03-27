package com.example.nagoyameshi.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReviewEditForm;
import com.example.nagoyameshi.form.ReviewRegisterForm;
import com.example.nagoyameshi.repository.RestaurantRepository;
import com.example.nagoyameshi.repository.ReviewRepository;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.ReviewService;

@Controller
@RequestMapping("/restaurants/{restaurantId}/reviews")
public class ReviewController {
	private static final String ROLE_FREE_MEMBER = "ROLE_FREE_MEMBER";
	private static final String SUBSCRIPTION_MESSAGE = "この機能を利用するには有料プランへの登録が必要です。";
	private static final String SUBSCRIPTION_URL = "/subscription/register";

	private final ReviewRepository reviewRepository;
	private final RestaurantRepository restaurantRepository;
	private final UserRepository userRepository;
	private final ReviewService reviewService;

	public ReviewController(ReviewRepository reviewRepository, RestaurantRepository restaurantRepository,
			UserRepository userRepository, ReviewService reviewService) {
		this.reviewRepository = reviewRepository;
		this.restaurantRepository = restaurantRepository;
		this.userRepository = userRepository;
		this.reviewService = reviewService;
	}

	@GetMapping
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@PathVariable(name = "restaurantId") Integer restaurantId,
			@PageableDefault(page = 0, size = 5, sort = "id", direction = Direction.ASC) Pageable pageable,
			Model model) {
		Page<Review> reviewPage;
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		String memberRole = user.getRole().getName();
		Restaurant restaurant = restaurantRepository.getReferenceById(restaurantId);

		if (memberRole.equals("ROLE_PAID_MEMBER")) {
			reviewPage = reviewRepository.findByRestaurantOrderByCreatedAtDesc(restaurant, pageable);
		} else {
			reviewPage = reviewRepository.findByRestaurantOrderByCreatedAtDesc(restaurant, PageRequest.of(0, 3));
		}

		boolean hasPostedReview = reviewService.hasUserAlreadyReviewed(restaurant, user);

		model.addAttribute("reviewPage", reviewPage);
		model.addAttribute("memberRole", memberRole);
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("hasPostedReview", hasPostedReview);

		return "reviews/index";
	}

	@GetMapping("/register")
	public String register(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@PathVariable(name = "restaurantId") Integer restaurantId,
			RedirectAttributes redirectAttributes,
			Model model) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());

		if (ROLE_FREE_MEMBER.equals(user.getRole().getName())) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", SUBSCRIPTION_MESSAGE);
			return "redirect:" + SUBSCRIPTION_URL;
		}

		Restaurant restaurant = restaurantRepository.getReferenceById(restaurantId);

		ReviewRegisterForm reviewRegisterForm = new ReviewRegisterForm();
		reviewRegisterForm.setScore(5);

		model.addAttribute("reviewRegisterForm", reviewRegisterForm);
		model.addAttribute("restaurant", restaurant);

		return "reviews/register";
	}

	@PostMapping("/create")
	public String create(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@PathVariable(name = "restaurantId") Integer restaurantId,
			@ModelAttribute @Validated ReviewRegisterForm reviewRegisterForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes,
			Model model) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());

		if (ROLE_FREE_MEMBER.equals(user.getRole().getName())) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", SUBSCRIPTION_MESSAGE);
			return "redirect:" + SUBSCRIPTION_URL;
		}

		Restaurant restaurant = restaurantRepository.getReferenceById(restaurantId);

		if (bindingResult.hasErrors()) {
			model.addAttribute("restaurant", restaurant);
			return "reviews/register";
		}

		reviewService.create(restaurant, user, reviewRegisterForm);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを投稿しました。");

		return "redirect:/restaurants/{restaurantId}/reviews";
	}

	@GetMapping("/{reviewId}/edit")
	public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@PathVariable(name = "restaurantId") Integer restaurantId,
			@PathVariable(name = "reviewId") Integer reviewId,
			RedirectAttributes redirectAttributes,
			Model model) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());

		if (ROLE_FREE_MEMBER.equals(user.getRole().getName())) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", SUBSCRIPTION_MESSAGE);
			return "redirect:" + SUBSCRIPTION_URL;
		}

		Review review = reviewRepository.findById(reviewId).orElse(null);
		/*        
		if (review == null || !review.getUser().equals(user)) {
		    return "redirect:/error";
		}
		*/
		Restaurant restaurant = restaurantRepository.getReferenceById(restaurantId);
		ReviewEditForm reviewEditForm = new ReviewEditForm(review.getId(), review.getScore(), review.getContent());

		model.addAttribute("restaurant", restaurant);
		model.addAttribute("review", review);
		model.addAttribute("reviewEditForm", reviewEditForm);

		return "reviews/edit";
	}

	@PostMapping("/{reviewId}/update")
	public String update(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@PathVariable(name = "restaurantId") Integer restaurantId,
			@PathVariable(name = "reviewId") Integer reviewId,
			@ModelAttribute @Validated ReviewEditForm reviewEditForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes,
			Model model) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());

		if (ROLE_FREE_MEMBER.equals(user.getRole().getName())) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", SUBSCRIPTION_MESSAGE);
			return "redirect:" + SUBSCRIPTION_URL;
		}

		Review review = reviewRepository.findById(reviewId).orElse(null);
		/*
		if (review == null || !review.getUser().equals(user)) {
		    return "redirect:/error";
		}
		*/
		Restaurant restaurant = restaurantRepository.getReferenceById(restaurantId);

		if (bindingResult.hasErrors()) {
			model.addAttribute("restaurant", restaurant);
			model.addAttribute("review", review);
			return "reviews/edit";
		}

		reviewService.update(reviewEditForm);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを編集しました。");

		return "redirect:/restaurants/{restaurantId}/reviews";
	}

	@PostMapping("/{reviewId}/delete")
	public String delete(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@PathVariable(name = "restaurantId") Integer restaurantId,
			@PathVariable(name = "reviewId") Integer reviewId,
			RedirectAttributes redirectAttributes) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());

		if (ROLE_FREE_MEMBER.equals(user.getRole().getName())) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", SUBSCRIPTION_MESSAGE);
			return "redirect:" + SUBSCRIPTION_URL;
		}

		Review review = reviewRepository.findById(reviewId).orElse(null);
		/*
		if (review == null || !review.getUser().equals(user)) {
		    return "redirect:/error";
		}
		*/
		reviewService.delete(review);

		redirectAttributes.addFlashAttribute("successMessage", "レビューを削除しました。");

		return "redirect:/restaurants/{restaurantId}/reviews";
	}
}
