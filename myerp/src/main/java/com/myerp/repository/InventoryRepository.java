package com.myerp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myerp.entity.InventoryDto;

public interface InventoryRepository extends JpaRepository<InventoryDto,Integer>{

}
