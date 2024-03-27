package com.example.nagoyameshi.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryEditForm {
	@NotNull
	private Integer id;

	@NotBlank
	private String name;
}
