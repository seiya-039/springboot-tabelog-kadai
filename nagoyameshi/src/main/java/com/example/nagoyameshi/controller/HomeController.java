package com.example.nagoyameshi.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.RestaurantRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;

@Controller
public class HomeController {
	private final RestaurantRepository restaurantRepository;

	public HomeController(RestaurantRepository restaurantRepository) {
		this.restaurantRepository = restaurantRepository;
	}

	@GetMapping("/")
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {

		Page<Restaurant> newRestaurants = restaurantRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 6));

		model.addAttribute("newRestaurants", newRestaurants);

		return "index";
	}
}