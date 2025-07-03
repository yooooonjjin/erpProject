package com.teamProject2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamProject2.entity.DailyStockDto;

public interface DailyStocktRepository extends JpaRepository<DailyStockDto, Integer> {

}
