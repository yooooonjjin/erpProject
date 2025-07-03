package com.teamProject2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamProject2.entity.MonthlyStockDto;

public interface MonthlyStockRepository extends JpaRepository<MonthlyStockDto, Integer> {

}
