package com.example.nagoyameshi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Company;
import com.example.nagoyameshi.form.CompanyEditForm;
import com.example.nagoyameshi.repository.CompanyRepository;

@Service
public class CompanyService {
	private final CompanyRepository companyRepository;

	public CompanyService(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
	}

	@Transactional
	public void update(CompanyEditForm companyEditForm) {
		Company company = companyRepository.getReferenceById(companyEditForm.getId());

		company.setName(companyEditForm.getName());
		company.setPostalCode(companyEditForm.getPostalCode());
		company.setAddress(companyEditForm.getAddress());
		company.setRepresentative(companyEditForm.getRepresentative());
		company.setEstablishmentDate(companyEditForm.getEstablishmentDate());
		company.setCapital(companyEditForm.getCapital());
		company.setBusiness(companyEditForm.getBusiness());
		company.setNumberOfEmployees(companyEditForm.getNumberOfEmployees());

		companyRepository.save(company);
	}
}
