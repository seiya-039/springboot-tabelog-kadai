package com.example.nagoyameshi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Integer> {
	public Company findFirstByOrderByIdAsc();
}
