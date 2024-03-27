package com.example.nagoyameshi.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.example.nagoyameshi.repository.ReservationRepository;
import com.example.nagoyameshi.repository.RestaurantRepository;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.RegularHolidayRestaurantService;
import com.example.nagoyameshi.service.ReservationService;

@Controller
public class ReservationController {
	private static final String ROLE_FREE_MEMBER = "ROLE_FREE_MEMBER";
	private static final String SUBSCRIPTION_MESSAGE = "この機能を利用するには有料プランへの登録が必要です。";
	private static final String SUBSCRIPTION_URL = "/subscription/register";

	private final ReservationRepository reservationRepository;
	private final RestaurantRepository restaurantRepository;
	private final UserRepository userRepository;
	private final ReservationService reservationService;
	private final RegularHolidayRestaurantService regularHolidayRestaurantService;

	public ReservationController(ReservationRepository reservationRepository,
			RestaurantRepository restaurantRepository,
			UserRepository userRepository,
			ReservationService reservationService,
			RegularHolidayRestaurantService regularHolidayRestaurantService) {
		this.reservationRepository = reservationRepository;
		this.restaurantRepository = restaurantRepository;
		this.userRepository = userRepository;
		this.reservationService = reservationService;
		this.regularHolidayRestaurantService = regularHolidayRestaurantService;
	}

	@GetMapping("/reservations")
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@PageableDefault(page = 0, size = 15, sort = "id") Pageable pageable, Model model) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		Page<Reservation> reservationPage = reservationRepository.findByUserOrderByReservedDatetimeDesc(user, pageable);

		model.addAttribute("reservationPage", reservationPage);
		model.addAttribute("currentDateTime", LocalDateTime.now());

		return "reservations/index";
	}

	@GetMapping("/restaurants/{restaurantId}/reservations/register")
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

		List<Integer> restaurantRegularHolidays = regularHolidayRestaurantService
				.getRestaurantRegularHolidays(restaurant);
		List<LocalTime> optionTimes = getOptionTimes(restaurant.getOpeningTime(), restaurant.getClosingTime(), 30);

		model.addAttribute("reservationRegisterForm", new ReservationRegisterForm());
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("restaurantRegularHolidays", restaurantRegularHolidays);
		model.addAttribute("optionTimes", optionTimes);

		return "reservations/register";
	}

	@PostMapping("/restaurants/{restaurantId}/reservations/create")
	public String create(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@PathVariable(name = "restaurantId") Integer restaurantId,
			@ModelAttribute @Validated ReservationRegisterForm reservationRegisterForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes,
			Model model) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());

		if (ROLE_FREE_MEMBER.equals(user.getRole().getName())) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", SUBSCRIPTION_MESSAGE);
			return "redirect:" + SUBSCRIPTION_URL;
		}

		Restaurant restaurant = restaurantRepository.getReferenceById(restaurantId);

		if (reservationRegisterForm.getReservationDate() != null
				&& reservationRegisterForm.getReservationTime() != null) {
			LocalDateTime reservationDateTime = LocalDateTime.of(reservationRegisterForm.getReservationDate(),
					reservationRegisterForm.getReservationTime());

			if (!reservationService.isAtLeastTwoHoursInFuture(reservationDateTime)) {
				FieldError fieldErrorTooSoon = new FieldError(bindingResult.getObjectName(), "reservationTime",
						"当日の予約は2時間前までにお願いいたします。");
				bindingResult.addError(fieldErrorTooSoon);
			}

			if (!reservationService.isTwoHoursApartFromOthers(reservationDateTime, user)) {
				FieldError fieldErrorAlreadyReserved = new FieldError(bindingResult.getObjectName(), "reservationTime",
						"その時間帯はすでに予約済みです。同日に予約する場合、少なくとも2時間以上空ける必要があります。");
				bindingResult.addError(fieldErrorAlreadyReserved);
			}

			int remainingSeats = reservationService.getRemainingSeats(reservationDateTime, restaurant);

			if (reservationRegisterForm.getNumberOfPeople() != null
					&& remainingSeats < reservationRegisterForm.getNumberOfPeople()) {
				if (remainingSeats == 0) {
					FieldError fieldErrorFull = new FieldError(bindingResult.getObjectName(), "numberOfPeople",
							"その時間帯は満席です。");
					bindingResult.addError(fieldErrorFull);
				} else {
					FieldError fieldErrorFewSeats = new FieldError(bindingResult.getObjectName(), "numberOfPeople",
							"その時間帯の残り予約可能人数は" + remainingSeats + "名です。");
					bindingResult.addError(fieldErrorFewSeats);
				}
			}
		}

		if (bindingResult.hasErrors()) {
			List<Integer> restaurantRegularHolidays = regularHolidayRestaurantService
					.getRestaurantRegularHolidays(restaurant);
			List<LocalTime> optionTimes = getOptionTimes(restaurant.getOpeningTime(), restaurant.getClosingTime(), 30);

			model.addAttribute("restaurant", restaurant);
			model.addAttribute("restaurantRegularHolidays", restaurantRegularHolidays);
			model.addAttribute("optionTimes", optionTimes);

			return "reservations/register";
		}

		reservationService.create(restaurant, user, reservationRegisterForm);
		redirectAttributes.addFlashAttribute("successMessage", "予約が完了しました。");

		return "redirect:/reservations";
	}

	@PostMapping("/reservations/{reservationId}/delete")
	public String delete(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@PathVariable(name = "reservationId") Integer reservationId,
			RedirectAttributes redirectAttributes) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());

		if (ROLE_FREE_MEMBER.equals(user.getRole().getName())) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", SUBSCRIPTION_MESSAGE);
			return "redirect:" + SUBSCRIPTION_URL;
		}

		Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
		/*
		if (reservation == null || !reservation.getUser().equals(user)) {
		    return "redirect:/error";
		}
		*/
		reservationService.delete(reservation);

		redirectAttributes.addFlashAttribute("successMessage", "予約をキャンセルしました。");

		return "redirect:/reservations";
	}

	private List<LocalTime> getOptionTimes(LocalTime start, LocalTime end, int intervalMinutes) {
		long minutesBetween = Duration.between(start, end).toMinutes();
		List<LocalTime> intervalTimes = new ArrayList<>();
		for (long i = 0; i <= minutesBetween; i += intervalMinutes) {
			intervalTimes.add(start.plusMinutes(i));
		}
		return intervalTimes;
	}
}
