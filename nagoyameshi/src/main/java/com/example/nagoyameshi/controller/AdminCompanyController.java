package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Company;
import com.example.nagoyameshi.form.CompanyEditForm;
import com.example.nagoyameshi.repository.CompanyRepository;
import com.example.nagoyameshi.service.CompanyService;

@Controller
@RequestMapping("/admin/company")
public class AdminCompanyController {
	private final CompanyRepository companyRepository;
	private final CompanyService companyService;

	public AdminCompanyController(CompanyRepository companyRepository, CompanyService companyService) {
		this.companyRepository = companyRepository;
		this.companyService = companyService;
	}

	@GetMapping
	public String index(Model model) {
		Company company = companyRepository.findFirstByOrderByIdAsc();

		model.addAttribute("company", company);

		return "admin/company/index";
	}

	@GetMapping("/{id}/edit")
	public String edit(@PathVariable(name = "id") Integer id, Model model) {
		Company company = companyRepository.getReferenceById(id);
		CompanyEditForm companyEditForm = new CompanyEditForm(company.getId(),
				company.getName(),
				company.getPostalCode(),
				company.getAddress(),
				company.getRepresentative(),
				company.getEstablishmentDate(),
				company.getCapital(),
				company.getBusiness(),
				company.getNumberOfEmployees());
		model.addAttribute("companyEditForm", companyEditForm);

		return "admin/company/edit";
	}

	@PostMapping("/{id}/update")
	public String update(@ModelAttribute @Validated CompanyEditForm companyEditForm, BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			return "admin/company/edit";
		}

		companyService.update(companyEditForm);
		redirectAttributes.addFlashAttribute("successMessage", "会社概要を編集しました。");

		return "redirect:/admin/company";
	}
}
