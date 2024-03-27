package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.nagoyameshi.entity.Company;
import com.example.nagoyameshi.repository.CompanyRepository;

@Controller
@RequestMapping("/company")
public class CompanyController {
	private final CompanyRepository companyRepository;

	public CompanyController(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
	}

	@GetMapping
	public String index(Model model) {
		Company company = companyRepository.findFirstByOrderByIdAsc();

		model.addAttribute("company", company);

		return "company/index";
	}
}
