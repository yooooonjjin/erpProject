package com.teamProject2.service;

import org.springframework.stereotype.Service;

import com.teamProject2.repository.ReasonRepository;


@Service
public class ReasonService {

	public final ReasonRepository reasonRepository;
	
	public ReasonService(ReasonRepository reasonRepository) {
		this.reasonRepository = reasonRepository;
	}
	
}
