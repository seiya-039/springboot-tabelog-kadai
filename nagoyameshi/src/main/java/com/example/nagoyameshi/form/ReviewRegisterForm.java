package com.example.nagoyameshi.form;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRegisterForm {
	@NotNull(message = "評価を選択してください。")
	@Range(min = 1, max = 5)
	private Integer score;

	@NotBlank(message = "感想を入力してください。")
	private String content;
}
