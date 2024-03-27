package com.example.nagoyameshi.controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.CategoryRestaurant;
import com.example.nagoyameshi.entity.RegularHoliday;
import com.example.nagoyameshi.entity.RegularHolidayRestaurant;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.form.RestaurantEditForm;
import com.example.nagoyameshi.form.RestaurantRegisterForm;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.CategoryRestaurantRepository;
import com.example.nagoyameshi.repository.RegularHolidayRepository;
import com.example.nagoyameshi.repository.RegularHolidayRestaurantRepository;
import com.example.nagoyameshi.repository.RestaurantRepository;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
@RequestMapping("/admin/restaurants")
public class AdminRestaurantController {
	// 最低価格と最高価格のセレクトボックスの範囲
	private final Integer LOWEST_PRICE_MIN = 500;
	private final Integer LOWEST_PRICE_MAX = 10000;
	private final Integer HIGHEST_PRICE_MIN = 500;
	private final Integer HIGHEST_PRICE_MAX = 10000;

	// 何円刻みにするか
	private final Integer PRICE_UNIT = 500;

	// 開店時間と閉店時間のセレクトボックスの範囲（単位：時）
	private final Integer OPENING_TIME_START = 0;
	private final Integer OPENING_TIME_END = 24;
	private final Integer CLOSING_TIME_START = 0;
	private final Integer CLOSING_TIME_END = 24;

	// 何分刻みにするか（単位：分）
	private final Integer TIME_UNIT = 30;

	private final RestaurantRepository restaurantRepository;
	private final RegularHolidayRepository regularHolidayRepository;
	private final RegularHolidayRestaurantRepository regularHolidayRestaurantRepository;
	private final CategoryRepository categoryRepository;
	private final CategoryRestaurantRepository categoryRestaurantRepository;
	private final RestaurantService restaurantService;

	public AdminRestaurantController(RestaurantRepository restaurantRepository,
			RegularHolidayRepository regularHolidayRepository,
			RegularHolidayRestaurantRepository regularHolidayRestaurantRepository,
			CategoryRepository categoryRepository,
			CategoryRestaurantRepository categoryRestaurantRepository,
			RestaurantService restaurantService) {
		this.restaurantRepository = restaurantRepository;
		this.regularHolidayRepository = regularHolidayRepository;
		this.regularHolidayRestaurantRepository = regularHolidayRestaurantRepository;
		this.categoryRepository = categoryRepository;
		this.categoryRestaurantRepository = categoryRestaurantRepository;
		this.restaurantService = restaurantService;
	}

	@GetMapping
	public String index(Model model,
			@PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC) Pageable pageable,
			@RequestParam(name = "keyword", required = false) String keyword) {
		Page<Restaurant> restaurantPage;

		if (keyword != null && !keyword.isEmpty()) {
			restaurantPage = restaurantRepository.findByNameLike("%" + keyword + "%", pageable);
		} else {
			restaurantPage = restaurantRepository.findAll(pageable);
		}

		model.addAttribute("restaurantPage", restaurantPage);
		model.addAttribute("keyword", keyword);

		return "admin/restaurants/index";
	}

	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id, Model model) {
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		List<RegularHolidayRestaurant> regularHolidayRestaurants = regularHolidayRestaurantRepository
				.findByRestaurantOrderByRegularHoliday_IdAsc(restaurant);
		List<CategoryRestaurant> categoryRestaurants = categoryRestaurantRepository
				.findByRestaurantOrderByIdAsc(restaurant);

		model.addAttribute("restaurant", restaurant);
		model.addAttribute("regularHolidayRestaurants", regularHolidayRestaurants);
		model.addAttribute("categoryRestaurants", categoryRestaurants);

		return "admin/restaurants/show";
	}

	@GetMapping("/register")
	public String register(Model model) {
		List<RegularHoliday> regularHolidays = regularHolidayRepository.findAll();
		List<Category> categories = categoryRepository.findAll();

		model.addAttribute("restaurantRegisterForm", new RestaurantRegisterForm());
		model.addAttribute("lowestPrices", generatePriceList(LOWEST_PRICE_MIN, LOWEST_PRICE_MAX, PRICE_UNIT));
		model.addAttribute("highestPrices", generatePriceList(HIGHEST_PRICE_MIN, HIGHEST_PRICE_MAX, PRICE_UNIT));
		model.addAttribute("openingTimes", generateTimeList(OPENING_TIME_START, OPENING_TIME_END, TIME_UNIT));
		model.addAttribute("closingTimes", generateTimeList(CLOSING_TIME_START, CLOSING_TIME_END, TIME_UNIT));
		model.addAttribute("regularHolidays", regularHolidays);
		model.addAttribute("categories", categories);

		return "admin/restaurants/register";
	}

	@PostMapping("/create")
	public String create(@ModelAttribute @Validated RestaurantRegisterForm restaurantRegisterForm,
			BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
		Integer lowestPrice = restaurantRegisterForm.getLowestPrice();
		Integer highestPrice = restaurantRegisterForm.getHighestPrice();
		LocalTime openingTime = restaurantRegisterForm.getOpeningTime();
		LocalTime closingTime = restaurantRegisterForm.getClosingTime();

		if (lowestPrice != null && highestPrice != null) {
			if (!restaurantService.isValidPrices(lowestPrice, highestPrice)) {
				FieldError fieldError1 = new FieldError(bindingResult.getObjectName(), "lowestPrice",
						"最低価格は最高価格以下に設定してください。");
				FieldError fieldError2 = new FieldError(bindingResult.getObjectName(), "highestPrice",
						"最高価格は最低価格以上に設定してください。");
				bindingResult.addError(fieldError1);
				bindingResult.addError(fieldError2);
			}
		}

		if (openingTime != null && closingTime != null) {
			if (!restaurantService.isValidBusinessHours(openingTime, closingTime)) {
				FieldError fieldError1 = new FieldError(bindingResult.getObjectName(), "openingTime",
						"開店時間は閉店時間よりも前に設定してください。");
				FieldError fieldError2 = new FieldError(bindingResult.getObjectName(), "closingTime",
						"閉店時間は開店時間よりも後に設定してください。");
				bindingResult.addError(fieldError1);
				bindingResult.addError(fieldError2);
			}
		}

		if (bindingResult.hasErrors()) {
			List<RegularHoliday> regularHolidays = regularHolidayRepository.findAll();
			List<Category> categories = categoryRepository.findAll();

			model.addAttribute("lowestPrices", generatePriceList(LOWEST_PRICE_MIN, LOWEST_PRICE_MAX, PRICE_UNIT));
			model.addAttribute("highestPrices", generatePriceList(HIGHEST_PRICE_MIN, HIGHEST_PRICE_MAX, PRICE_UNIT));
			model.addAttribute("openingTimes", generateTimeList(OPENING_TIME_START, OPENING_TIME_END, TIME_UNIT));
			model.addAttribute("closingTimes", generateTimeList(CLOSING_TIME_START, CLOSING_TIME_END, TIME_UNIT));
			model.addAttribute("regularHolidays", regularHolidays);
			model.addAttribute("categories", categories);

			return "admin/restaurants/register";
		}

		restaurantService.create(restaurantRegisterForm);
		redirectAttributes.addFlashAttribute("successMessage", "店舗を登録しました。");

		return "redirect:/admin/restaurants";
	}

	@GetMapping("/{id}/edit")
	public String edit(@PathVariable(name = "id") Integer id, Model model) {
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		String image = restaurant.getImage();
		List<Integer> regularHolidayIds = regularHolidayRestaurantRepository
				.findRegularHolidayIdsByRestaurantOrderByRegularHoliday_IdAsc(restaurant);
		List<Integer> categoryIds = categoryRestaurantRepository.findCategoryIdsByRestaurantOrderByIdAsc(restaurant);
		RestaurantEditForm restaurantEditForm = new RestaurantEditForm(restaurant.getId(),
				restaurant.getName(),
				null,
				restaurant.getDescription(),
				restaurant.getLowestPrice(),
				restaurant.getHighestPrice(),
				restaurant.getPostalCode(),
				restaurant.getAddress(),
				restaurant.getOpeningTime(),
				restaurant.getClosingTime(),
				regularHolidayIds,
				restaurant.getSeatingCapacity(),
				categoryIds);
		List<RegularHoliday> regularHolidays = regularHolidayRepository.findAll();
		List<Category> categories = categoryRepository.findAll();

		model.addAttribute("image", image);
		model.addAttribute("restaurantEditForm", restaurantEditForm);
		model.addAttribute("lowestPrices", generatePriceList(LOWEST_PRICE_MIN, LOWEST_PRICE_MAX, PRICE_UNIT));
		model.addAttribute("highestPrices", generatePriceList(HIGHEST_PRICE_MIN, HIGHEST_PRICE_MAX, PRICE_UNIT));
		model.addAttribute("openingTimes", generateTimeList(OPENING_TIME_START, OPENING_TIME_END, TIME_UNIT));
		model.addAttribute("closingTimes", generateTimeList(CLOSING_TIME_START, CLOSING_TIME_END, TIME_UNIT));
		model.addAttribute("regularHolidays", regularHolidays);
		model.addAttribute("categories", categories);

		return "admin/restaurants/edit";
	}

	@PostMapping("/{id}/update")
	public String update(@ModelAttribute @Validated RestaurantEditForm restaurantEditForm, BindingResult bindingResult,
			RedirectAttributes redirectAttributes, Model model) {
		Integer lowestPrice = restaurantEditForm.getLowestPrice();
		Integer highestPrice = restaurantEditForm.getHighestPrice();
		LocalTime openingTime = restaurantEditForm.getOpeningTime();
		LocalTime closingTime = restaurantEditForm.getClosingTime();

		if (lowestPrice != null && highestPrice != null) {
			if (!restaurantService.isValidPrices(lowestPrice, highestPrice)) {
				FieldError fieldError1 = new FieldError(bindingResult.getObjectName(), "lowestPrice",
						"最低価格は最高価格以下に設定してください。");
				FieldError fieldError2 = new FieldError(bindingResult.getObjectName(), "highestPrice",
						"最高価格は最低価格以上に設定してください。");
				bindingResult.addError(fieldError1);
				bindingResult.addError(fieldError2);
			}
		}

		if (openingTime != null && closingTime != null) {
			if (!restaurantService.isValidBusinessHours(openingTime, closingTime)) {
				FieldError fieldError1 = new FieldError(bindingResult.getObjectName(), "openingTime",
						"開店時間は閉店時間よりも前に設定してください。");
				FieldError fieldError2 = new FieldError(bindingResult.getObjectName(), "closingTime",
						"閉店時間は開店時間よりも後に設定してください。");
				bindingResult.addError(fieldError1);
				bindingResult.addError(fieldError2);
			}
		}

		if (bindingResult.hasErrors()) {
			List<RegularHoliday> regularHolidays = regularHolidayRepository.findAll();
			List<Category> categories = categoryRepository.findAll();

			model.addAttribute("lowestPrices", generatePriceList(LOWEST_PRICE_MIN, LOWEST_PRICE_MAX, PRICE_UNIT));
			model.addAttribute("highestPrices", generatePriceList(HIGHEST_PRICE_MIN, HIGHEST_PRICE_MAX, PRICE_UNIT));
			model.addAttribute("openingTimes", generateTimeList(OPENING_TIME_START, OPENING_TIME_END, TIME_UNIT));
			model.addAttribute("closingTimes", generateTimeList(CLOSING_TIME_START, CLOSING_TIME_END, TIME_UNIT));
			model.addAttribute("regularHolidays", regularHolidays);
			model.addAttribute("categories", categories);

			return "admin/restaurants/edit";
		}

		restaurantService.update(restaurantEditForm);
		redirectAttributes.addFlashAttribute("successMessage", "店舗を編集しました。");

		return "redirect:/admin/restaurants";
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		restaurantService.delete(restaurant);

		redirectAttributes.addFlashAttribute("successMessage", "店舗を削除しました。");

		return "redirect:/admin/restaurants";
	}

	private List<Integer> generatePriceList(Integer min, Integer max, Integer unit) {
		List<Integer> prices = new ArrayList<>();
		for (int i = 0; i <= (max - min) / unit; i++) {
			int price = min + (unit * i);
			prices.add(price);
		}
		return prices;
	}

	private List<String> generateTimeList(Integer start, Integer end, Integer unit) {
		List<String> times = new ArrayList<>();
		for (int i = start * 60; i < end * 60; i += unit) {
			String time = LocalTime.MIN.plusMinutes(i).format(DateTimeFormatter.ofPattern("HH:mm"));
			times.add(time);
		}
		return times;
	}
}
