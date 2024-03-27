package com.example.nagoyameshi.form;

import java.time.LocalDate;
import java.time.LocalTime;

import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationRegisterForm {
	@NotNull(message = "予約日を選択してください。")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate reservationDate;

	@NotNull(message = "時間を選択してください。")
	@DateTimeFormat(pattern = "HH:mm:ss")
	private LocalTime reservationTime;

	@NotNull(message = "人数を選択してください。")
	@Range(min = 1, max = 50)
	private Integer numberOfPeople;
}
