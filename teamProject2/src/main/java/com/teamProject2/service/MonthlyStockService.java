package com.teamProject2.service;

import org.springframework.stereotype.Service;

import com.teamProject2.repository.MonthlyStockRepository;

@Service
public class MonthlyStockService {
	
	public final MonthlyStockRepository monthlyStockRepository;
	
	public MonthlyStockService(MonthlyStockRepository monthlyStockRepository) {
		this.monthlyStockRepository = monthlyStockRepository;
	}
	

}
