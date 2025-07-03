package com.myerp.service;

import org.springframework.stereotype.Service;

import com.myerp.repository.ReasonRepository;


@Service
public class ReasonService {

	public final ReasonRepository reasonRepository;
	
	public ReasonService(ReasonRepository reasonRepository) {
		this.reasonRepository = reasonRepository;
	}
	
}
