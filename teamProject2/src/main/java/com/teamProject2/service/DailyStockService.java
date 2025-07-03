package com.teamProject2.service;

import org.springframework.stereotype.Service;

import com.teamProject2.repository.DailyStocktRepository;

@Service
public class DailyStockService {

	public final DailyStocktRepository dailyStocktRepository;
	
	public DailyStockService(DailyStocktRepository dailyStocktRepository) {
		this.dailyStocktRepository = dailyStocktRepository;
	}
}
