package com.teamProject2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamProject2.entity.InventoryDto;

public interface InventoryRepository extends JpaRepository<InventoryDto,Integer>{

}
