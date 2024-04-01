package com.example.nagoyameshi.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.CategoryRestaurant;
import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.RegularHolidayRestaurant;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.CategoryRestaurantRepository;
import com.example.nagoyameshi.repository.FavoriteRepository;
import com.example.nagoyameshi.repository.RegularHolidayRestaurantRepository;
import com.example.nagoyameshi.repository.RestaurantRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.FavoriteService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
@RequestMapping("/restaurants")
public class RestaurantController {
	// 予算のセレクトボックスの範囲
	private final Integer PRICE_MIN = 500;
	private final Integer PRICE_MAX = 10000;

	// 何円刻みにするか
	private final Integer PRICE_UNIT = 500;

	private final RestaurantRepository restaurantRepository;
	private final RegularHolidayRestaurantRepository regularHolidayRestaurantRepository;
	private final CategoryRepository categoryRepository;
	private final CategoryRestaurantRepository categoryRestaurantRepository;
	private final RestaurantService restaurantService;
	private FavoriteRepository favoriteRepository;
	private FavoriteService favoriteService;

	public RestaurantController(RestaurantRepository restaurantRepository,
			RegularHolidayRestaurantRepository regularHolidayRestaurantRepository,
			CategoryRepository categoryRepository,
			CategoryRestaurantRepository categoryRestaurantRepository,
			RestaurantService restaurantService, FavoriteRepository favoriteRepository,
			FavoriteService favoriteService) {
		this.restaurantRepository = restaurantRepository;
		this.regularHolidayRestaurantRepository = regularHolidayRestaurantRepository;
		this.categoryRepository = categoryRepository;
		this.categoryRestaurantRepository = categoryRestaurantRepository;
		this.restaurantService = restaurantService;
		this.favoriteRepository = favoriteRepository;
		this.favoriteService = favoriteService;

	}

	@GetMapping
	public String index(Model model,
			@PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC) Pageable pageable,
			@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "categoryId", required = false) Integer categoryId,
			@RequestParam(name = "price", required = false) Integer price,
			@RequestParam(name = "order", required = false) String order) {
		List<Restaurant> restaurants;
		String sortMethod = order != null ? order : "createdAtDesc";

		if (keyword != null && !keyword.isEmpty()) {
			restaurants = restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLike("%" + keyword + "%");
		} else if (categoryId != null) {
			restaurants = restaurantRepository.findByCategoryId(categoryId);
		} else if (price != null) {
			restaurants = restaurantRepository.findByLowestPriceLessThanEqual(price);
		} else {
			restaurants = restaurantRepository.findAll();
		}

		List<Restaurant> sortedRestaurants;

		switch (sortMethod) {
		case "priceAsc":
			sortedRestaurants = restaurantService.sortByPriceAsc(restaurants);
			break;
		default:
			sortedRestaurants = restaurantService.sortByCreatedAtDesc(restaurants);
			break;
		}

		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), sortedRestaurants.size());
		List<Restaurant> pagedList = sortedRestaurants.subList(start, end);
		Page<Restaurant> restaurantPage = new PageImpl<>(pagedList, pageable, sortedRestaurants.size());

		List<Category> categories = categoryRepository.findAll();

		model.addAttribute("restaurantPage", restaurantPage);
		model.addAttribute("categories", categories);
		model.addAttribute("optionPrices", generatePriceList(PRICE_MIN, PRICE_MAX, PRICE_UNIT));
		model.addAttribute("keyword", keyword);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("price", price);
		model.addAttribute("order", order);

		return "restaurants/index";
	}

	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id, Model model,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		Favorite favorite = null;
		boolean isFavorite = false;
		List<RegularHolidayRestaurant> regularHolidayRestaurants = regularHolidayRestaurantRepository
				.findByRestaurantOrderByRegularHoliday_IdAsc(restaurant);
		List<CategoryRestaurant> categoryRestaurants = categoryRestaurantRepository
				.findByRestaurantOrderByIdAsc(restaurant);
		
		if (userDetailsImpl != null) {
			User user = userDetailsImpl.getUser();

			isFavorite = favoriteService.isFavorite(restaurant, user);
			if (isFavorite) {
				favorite = favoriteRepository.findByRestaurantAndUser(restaurant, user);
			}
			}
			model.addAttribute("restaurant", restaurant);
			model.addAttribute("favorite", favorite);
			model.addAttribute("regularHolidayRestaurants", regularHolidayRestaurants);
			model.addAttribute("categoryRestaurants", categoryRestaurants);
			model.addAttribute("isFavorite", isFavorite);

			return "restaurants/show";
	}

	private List<Integer> generatePriceList(Integer min, Integer max, Integer unit) {
		List<Integer> prices = new ArrayList<>();
		for (int i = 0; i <= (max - min) / unit; i++) {
			int price = min + (unit * i);
			prices.add(price);
		}
		return prices;
	}
}
