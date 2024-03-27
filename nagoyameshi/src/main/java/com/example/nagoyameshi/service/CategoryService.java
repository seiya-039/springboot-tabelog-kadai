package com.example.nagoyameshi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.form.CategoryEditForm;
import com.example.nagoyameshi.form.CategoryRegisterForm;
import com.example.nagoyameshi.repository.CategoryRepository;

@Service
public class CategoryService {
	private final CategoryRepository categoryRepository;
	private final CategoryRestaurantService categoryRestaurantService;

	public CategoryService(CategoryRepository categoryRepository, CategoryRestaurantService categoryRestaurantService) {
		this.categoryRepository = categoryRepository;
		this.categoryRestaurantService = categoryRestaurantService;
	}

	@Transactional
	public void create(CategoryRegisterForm categoryRegisterForm) {
		Category category = new Category();

		category.setName(categoryRegisterForm.getName());

		categoryRepository.save(category);
	}

	@Transactional
	public void update(CategoryEditForm categoryEditForm) {
		Category category = categoryRepository.getReferenceById(categoryEditForm.getId());

		category.setName(categoryEditForm.getName());

		categoryRepository.save(category);
	}

	@Transactional
	public void delete(Category category) {
		categoryRestaurantService.deleteByCategory(category);
		categoryRepository.delete(category);
	}
}
