package com.myerp.service;

import org.springframework.stereotype.Service;

import com.myerp.repository.MonthlyStockRepository;

@Service
public class MonthlyStockService {
	
	public final MonthlyStockRepository monthlyStockRepository;
	
	public MonthlyStockService(MonthlyStockRepository monthlyStockRepository) {
		this.monthlyStockRepository = monthlyStockRepository;
	}
	

}
