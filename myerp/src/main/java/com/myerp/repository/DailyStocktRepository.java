package com.myerp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myerp.entity.DailyStockDto;

public interface DailyStocktRepository extends JpaRepository<DailyStockDto, Integer> {

}
