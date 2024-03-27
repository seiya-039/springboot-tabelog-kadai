package com.example.nagoyameshi.form;

import java.time.LocalTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RestaurantEditForm {
	@NotNull
	private Integer id;

	@NotBlank(message = "店舗名を入力してください。")
	private String name;

	private MultipartFile image;

	@NotBlank(message = "説明を入力してください。")
	private String description;

	@NotNull(message = "最低価格を選択してください。")
	private Integer lowestPrice;

	@NotNull(message = "最高価格を選択してください。")
	private Integer highestPrice;

	@NotBlank(message = "郵便番号を入力してください。")
	@Pattern(regexp = "^[0-9]{7}$", message = "郵便番号は7桁の半角数字で入力してください。")
	private String postalCode;

	@NotBlank(message = "住所を入力してください。")
	private String address;

	@NotNull(message = "開店時間を選択してください。")
	private LocalTime openingTime;

	@NotNull(message = "閉店時間を選択してください。")
	private LocalTime closingTime;

	private List<Integer> regularHolidayIds;

	@NotNull(message = "座席数を入力してください。")
	@Min(value = 0, message = "座席数は0席以上に設定してください。")
	private Integer seatingCapacity;

	private List<Integer> categoryIds;
}
