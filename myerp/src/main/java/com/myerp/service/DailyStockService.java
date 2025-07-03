package com.myerp.service;

import org.springframework.stereotype.Service;

import com.myerp.repository.DailyStocktRepository;

@Service
public class DailyStockService {

	public final DailyStocktRepository dailyStocktRepository;
	
	public DailyStockService(DailyStocktRepository dailyStocktRepository) {
		this.dailyStocktRepository = dailyStocktRepository;
	}
}
