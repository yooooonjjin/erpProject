package com.myerp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myerp.entity.MonthlyStockDto;

public interface MonthlyStockRepository extends JpaRepository<MonthlyStockDto, Integer> {

}
