package com.fitwise.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fitwise.entity.ExpertiseLevels;
import com.fitwise.repository.ExpertiseLevelRepository;

@Service
public class ExpertiseLevelService {

	@Autowired
	ExpertiseLevelRepository repository;
	
	public List<ExpertiseLevels> getExpertiseLevelList(){
		return repository.findAll();
	}
	
}
